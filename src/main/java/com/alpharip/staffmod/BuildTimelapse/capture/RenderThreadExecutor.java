package com.alpharip.staffmod.BuildTimelapse.capture;
import java.util.concurrent.Callable;
public interface RenderThreadExecutor {<T>T call(Callable<T> task)throws Exception;}