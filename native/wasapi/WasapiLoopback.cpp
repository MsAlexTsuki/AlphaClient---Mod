#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>
#include <mmdeviceapi.h>
#include <audioclient.h>
#include <ksmedia.h>
#include <jni.h>
#include <atomic>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <limits>
#include <string>
#include <vector>

#pragma comment(lib, "ole32.lib")

namespace {
std::atomic<bool> g_stop(false);

template <typename T>
void releaseCom(T*& value) {
    if (value) {
        value->Release();
        value = nullptr;
    }
}

void throwIOException(JNIEnv* env, const char* message) {
    jclass type = env->FindClass("java/io/IOException");
    if (type) env->ThrowNew(type, message);
}

bool writeAll(HANDLE file, const void* data, DWORD size) {
    const BYTE* cursor = static_cast<const BYTE*>(data);
    while (size > 0) {
        DWORD written = 0;
        if (!WriteFile(file, cursor, size, &written, nullptr) || written == 0) return false;
        cursor += written;
        size -= written;
    }
    return true;
}

void put16(BYTE* out, uint16_t value) {
    out[0] = static_cast<BYTE>(value);
    out[1] = static_cast<BYTE>(value >> 8);
}

void put32(BYTE* out, uint32_t value) {
    out[0] = static_cast<BYTE>(value);
    out[1] = static_cast<BYTE>(value >> 8);
    out[2] = static_cast<BYTE>(value >> 16);
    out[3] = static_cast<BYTE>(value >> 24);
}

void makeWaveHeader(BYTE header[44], uint32_t sampleRate, uint16_t channels, uint32_t dataBytes) {
    ZeroMemory(header, 44);
    memcpy(header, "RIFF", 4);
    put32(header + 4, 36U + dataBytes);
    memcpy(header + 8, "WAVEfmt ", 8);
    put32(header + 16, 16);
    put16(header + 20, 1);
    put16(header + 22, channels);
    put32(header + 24, sampleRate);
    put32(header + 28, sampleRate * channels * 2U);
    put16(header + 32, static_cast<uint16_t>(channels * 2U));
    put16(header + 34, 16);
    memcpy(header + 36, "data", 4);
    put32(header + 40, dataBytes);
}

bool isFloatFormat(const WAVEFORMATEX* format) {
    if (format->wFormatTag == WAVE_FORMAT_IEEE_FLOAT) return true;
    if (format->wFormatTag == WAVE_FORMAT_EXTENSIBLE && format->cbSize >= 22) {
        const WAVEFORMATEXTENSIBLE* extended = reinterpret_cast<const WAVEFORMATEXTENSIBLE*>(format);
        return IsEqualGUID(extended->SubFormat, KSDATAFORMAT_SUBTYPE_IEEE_FLOAT) != FALSE;
    }
    return false;
}

bool supportedFormat(const WAVEFORMATEX* format) {
    if (!format || format->nChannels == 0 || format->nChannels > 8
            || format->nSamplesPerSec < 8000 || format->nSamplesPerSec > 384000) return false;
    if (format->wFormatTag == WAVE_FORMAT_EXTENSIBLE) {
        if (format->cbSize < 22) return false;
        const WAVEFORMATEXTENSIBLE* extended = reinterpret_cast<const WAVEFORMATEXTENSIBLE*>(format);
        if (!IsEqualGUID(extended->SubFormat, KSDATAFORMAT_SUBTYPE_IEEE_FLOAT)
                && !IsEqualGUID(extended->SubFormat, KSDATAFORMAT_SUBTYPE_PCM)) return false;
    } else if (format->wFormatTag != WAVE_FORMAT_IEEE_FLOAT && format->wFormatTag != WAVE_FORMAT_PCM) {
        return false;
    }
    const bool floatingPoint = isFloatFormat(format);
    const uint16_t bits = format->wBitsPerSample;
    if (floatingPoint) return bits == 32 || bits == 64;
    return bits == 8 || bits == 16 || bits == 24 || bits == 32;
}

double readSample(const BYTE* data, const WAVEFORMATEX* format, bool floatingPoint) {
    const uint16_t bits = format->wBitsPerSample;
    const size_t bytesPerSample = bits / 8;
    if (floatingPoint) {
        if (bits == 32) {
            float value;
            memcpy(&value, data, sizeof(value));
            return std::max(-1.0, std::min(1.0, static_cast<double>(value)));
        }
        if (bits == 64) {
            double value;
            memcpy(&value, data, sizeof(value));
            return std::max(-1.0, std::min(1.0, value));
        }
        return 0.0;
    }
    if (bits == 8) return (static_cast<int>(data[0]) - 128) / 128.0;
    if (bits == 16) {
        int16_t value;
        memcpy(&value, data, sizeof(value));
        return value < 0 ? value / 32768.0 : value / 32767.0;
    }
    if (bits == 24) {
        int32_t value = static_cast<int32_t>(data[0])
                | (static_cast<int32_t>(data[1]) << 8)
                | (static_cast<int32_t>(data[2]) << 16);
        if (value & 0x00800000) value |= static_cast<int32_t>(0xFF000000);
        return value < 0 ? value / 8388608.0 : value / 8388607.0;
    }
    if (bits == 32) {
        int32_t value;
        memcpy(&value, data, sizeof(value));
        return value < 0 ? value / 2147483648.0 : value / 2147483647.0;
    }
    (void) bytesPerSample;
    return 0.0;
}

bool convertPacket(const BYTE* input, UINT32 frames, const WAVEFORMATEX* format,
                   bool floatingPoint, std::vector<int16_t>& output) {
    const uint16_t channels = format->nChannels;
    const size_t bytesPerSample = format->wBitsPerSample / 8;
    const size_t expectedFrameBytes = bytesPerSample * channels;
    if (expectedFrameBytes == 0 || format->nBlockAlign < expectedFrameBytes) return false;
    output.resize(static_cast<size_t>(frames) * channels);
    for (UINT32 frame = 0; frame < frames; ++frame) {
        const BYTE* frameData = input + static_cast<size_t>(frame) * format->nBlockAlign;
        for (uint16_t channel = 0; channel < channels; ++channel) {
            double sample = readSample(frameData + channel * bytesPerSample, format, floatingPoint);
            long converted = static_cast<long>(std::lrint(sample * 32767.0));
            converted = std::max(-32768L, std::min(32767L, converted));
            output[static_cast<size_t>(frame) * channels + channel] = static_cast<int16_t>(converted);
        }
    }
    return true;
}
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_alpharip_staffmod_RecordingStudio_audio_WasapiNativeBridge_nativeCapture(
        JNIEnv* env, jclass, jstring temporaryPath, jstring outputPath) {
    if (!temporaryPath || !outputPath) {
        throwIOException(env, "WASAPI output paths are required");
        return 0;
    }
    const jchar* temporaryChars = env->GetStringChars(temporaryPath, nullptr);
    const jchar* outputChars = env->GetStringChars(outputPath, nullptr);
    if (!temporaryChars || !outputChars) {
        if (temporaryChars) env->ReleaseStringChars(temporaryPath, temporaryChars);
        if (outputChars) env->ReleaseStringChars(outputPath, outputChars);
        return 0;
    }
    std::wstring temporary(reinterpret_cast<const wchar_t*>(temporaryChars), env->GetStringLength(temporaryPath));
    std::wstring output(reinterpret_cast<const wchar_t*>(outputChars), env->GetStringLength(outputPath));
    env->ReleaseStringChars(temporaryPath, temporaryChars);
    env->ReleaseStringChars(outputPath, outputChars);

    HRESULT comResult = CoInitializeEx(nullptr, COINIT_MULTITHREADED);
    const bool uninitializeCom = SUCCEEDED(comResult);
    if (FAILED(comResult) && comResult != RPC_E_CHANGED_MODE) {
        throwIOException(env, "Could not initialize Windows audio services");
        return 0;
    }

    IMMDeviceEnumerator* enumerator = nullptr;
    IMMDevice* device = nullptr;
    IAudioClient* audioClient = nullptr;
    IAudioCaptureClient* captureClient = nullptr;
    WAVEFORMATEX* mixFormat = nullptr;
    HANDLE file = INVALID_HANDLE_VALUE;
    HRESULT result = CoCreateInstance(__uuidof(MMDeviceEnumerator), nullptr, CLSCTX_ALL,
                                      __uuidof(IMMDeviceEnumerator), reinterpret_cast<void**>(&enumerator));
    const char* failure = nullptr;
    uint32_t dataBytes = 0;
    uint32_t sampleRate = 0;
    uint16_t channels = 0;
    bool floatingPoint = false;

    if (FAILED(result)) failure = "Could not create the Windows audio device enumerator";
    if (!failure) {
        result = enumerator->GetDefaultAudioEndpoint(eRender, eConsole, &device);
        if (FAILED(result)) failure = "No default Windows audio output device is available";
    }
    if (!failure) {
        result = device->Activate(__uuidof(IAudioClient), CLSCTX_ALL, nullptr,
                                  reinterpret_cast<void**>(&audioClient));
        if (FAILED(result)) failure = "Could not open the default Windows audio output";
    }
    if (!failure) {
        result = audioClient->GetMixFormat(&mixFormat);
        if (FAILED(result) || !supportedFormat(mixFormat)) {
            failure = "The default Windows audio format is not supported by WASAPI capture";
        } else {
            sampleRate = mixFormat->nSamplesPerSec;
            channels = mixFormat->nChannels;
            floatingPoint = isFloatFormat(mixFormat);
        }
    }
    if (!failure) {
        result = audioClient->Initialize(AUDCLNT_SHAREMODE_SHARED, AUDCLNT_STREAMFLAGS_LOOPBACK,
                                         10000000, 0, mixFormat, nullptr);
        if (FAILED(result)) failure = "Could not initialize WASAPI loopback on the default output";
    }
    if (!failure) {
        result = audioClient->GetService(__uuidof(IAudioCaptureClient),
                                         reinterpret_cast<void**>(&captureClient));
        if (FAILED(result)) failure = "Could not initialize the WASAPI capture stream";
    }
    if (!failure) {
        file = CreateFileW(temporary.c_str(), GENERIC_WRITE, 0, nullptr, CREATE_NEW,
                           FILE_ATTRIBUTE_NORMAL, nullptr);
        if (file == INVALID_HANDLE_VALUE) failure = "Could not create the temporary game-audio WAV file";
    }
    if (!failure) {
        BYTE header[44];
        makeWaveHeader(header, sampleRate, channels, 0);
        if (!writeAll(file, header, sizeof(header))) failure = "Could not write the game-audio WAV header";
    }
    if (!failure) {
        result = audioClient->Start();
        if (FAILED(result)) failure = "WASAPI could not start capturing the Windows audio output";
    }

    std::vector<int16_t> converted;
    while (!failure && !g_stop.load()) {
        UINT32 packetFrames = 0;
        result = captureClient->GetNextPacketSize(&packetFrames);
        if (FAILED(result)) {
            failure = "WASAPI could not read the next audio packet";
            break;
        }
        if (packetFrames == 0) {
            Sleep(5);
            continue;
        }
        while (packetFrames > 0 && !failure && !g_stop.load()) {
            BYTE* packetData = nullptr;
            UINT32 frames = 0;
            DWORD flags = 0;
            result = captureClient->GetBuffer(&packetData, &frames, &flags, nullptr, nullptr);
            if (FAILED(result)) {
                failure = "WASAPI could not retrieve an audio packet";
                break;
            }
            if (flags & AUDCLNT_BUFFERFLAGS_SILENT) {
                converted.assign(static_cast<size_t>(frames) * channels, 0);
            } else if (!convertPacket(packetData, frames, mixFormat, floatingPoint, converted)) {
                failure = "WASAPI returned an audio packet with an unsupported format";
            }
            if (!failure) {
                const uint64_t packetBytes = static_cast<uint64_t>(converted.size()) * sizeof(int16_t);
                if (packetBytes > std::numeric_limits<DWORD>::max()
                        || packetBytes > 0xFFFFFF00ULL - dataBytes) {
                    failure = "WASAPI recording reached the WAV file size limit";
                } else if (packetBytes > 0 && !writeAll(file, converted.data(), static_cast<DWORD>(packetBytes))) {
                    failure = "Could not write captured game audio to disk";
                } else {
                    dataBytes += static_cast<uint32_t>(packetBytes);
                }
            }
            HRESULT releaseResult = captureClient->ReleaseBuffer(frames);
            if (FAILED(releaseResult) && !failure) failure = "WASAPI could not release an audio packet";
            if (failure) break;
            result = captureClient->GetNextPacketSize(&packetFrames);
            if (FAILED(result)) {
                failure = "WASAPI could not continue reading audio packets";
                break;
            }
        }
    }

    if (audioClient) audioClient->Stop();
    if (file != INVALID_HANDLE_VALUE) {
        if (!failure && dataBytes == 0) failure = "No game audio was captured";
        if (!failure) {
            BYTE header[44];
            makeWaveHeader(header, sampleRate, channels, dataBytes);
            LARGE_INTEGER beginning;
            beginning.QuadPart = 0;
            if (!SetFilePointerEx(file, beginning, nullptr, FILE_BEGIN)
                    || !writeAll(file, header, sizeof(header)) || !FlushFileBuffers(file)) {
                failure = "Could not finalize the game-audio WAV file";
            }
        }
        CloseHandle(file);
        file = INVALID_HANDLE_VALUE;
    }
    if (!failure && !MoveFileExW(temporary.c_str(), output.c_str(), MOVEFILE_WRITE_THROUGH)) {
        failure = "Could not finalize the captured game-audio file";
    }
    if (failure) DeleteFileW(temporary.c_str());

    CoTaskMemFree(mixFormat);
    releaseCom(captureClient);
    releaseCom(audioClient);
    releaseCom(device);
    releaseCom(enumerator);
    if (uninitializeCom) CoUninitialize();

    if (failure) {
        throwIOException(env, failure);
        return 0;
    }
    const uint32_t bytesPerSecond = sampleRate * channels * 2U;
    return bytesPerSecond == 0 ? 0 : static_cast<jlong>(dataBytes) * 1000LL / bytesPerSecond;
}

extern "C" JNIEXPORT void JNICALL
Java_com_alpharip_staffmod_RecordingStudio_audio_WasapiNativeBridge_nativeReset(
        JNIEnv*, jclass) {
    g_stop.store(false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_alpharip_staffmod_RecordingStudio_audio_WasapiNativeBridge_nativeStop(
        JNIEnv*, jclass) {
    g_stop.store(true);
}
