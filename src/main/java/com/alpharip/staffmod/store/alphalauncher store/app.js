const products = [
  {id:"aurora-cape",name:"Aurora Cape",category:"capes",label:"CAPE",description:"A clean gold-accented cape concept for the Alpha collection.",glyph:"A",art:"cape",badge:"FEATURED"},
  {id:"ember-cape",name:"Ember Cape",category:"capes",label:"CAPE",description:"A warm, ember-inspired cape concept.",glyph:"✦",art:"cape",badge:"PREVIEW"},
  {id:"wave-emote",name:"Wave Emote",category:"emotes",label:"EMOTE",description:"A simple greeting emote concept for player expression.",glyph:"↗",art:"emote",badge:"PREVIEW"},
  {id:"alpha-tag",name:"Alpha Nameplate",category:"nametags",label:"NAMETAG",description:"A distinctive Alpha-styled nameplate treatment.",glyph:"ALPHA",art:"name",badge:"PREVIEW"},
  {id:"spark-effect",name:"Golden Spark",category:"effects",label:"EFFECT",description:"A subtle gold particle-effect concept.",glyph:"✧",art:"effect",badge:"PREVIEW"},
  {id:"dark-ui",name:"Obsidian UI",category:"ui",label:"UI STYLE",description:"A dark interface accent concept for the Alpha experience.",glyph:"A",art:"ui",badge:"PREVIEW"}
];

const grid = document.getElementById("product-grid");
const searchInput = document.getElementById("search-input");
const emptyState = document.getElementById("empty-state");
const modal = document.getElementById("modal");
let activeCategory = "all";
let activeView = "discover";

function renderProducts() {
  const query = searchInput.value.trim().toLowerCase();
  const visible = products.filter((item) => {
    const categoryMatch = activeCategory === "all" || item.category === activeCategory;
    const searchMatch = !query || [item.name, item.label, item.description].join(" ").toLowerCase().includes(query);
    return categoryMatch && searchMatch;
  });
  grid.innerHTML = visible.map((item) => `
    <article class="product-card">
      <div class="product-art" data-art="${item.art}">
        <span class="product-badge">${item.badge}</span>
        <span class="product-glyph">${item.glyph}</span>
      </div>
      <div class="product-meta">
        <div class="product-category">${item.label}</div>
        <h3 class="product-name">${item.name}</h3>
        <p class="product-desc">${item.description}</p>
        <div class="card-bottom"><button class="preview-link" data-preview="${item.id}">View preview ↗</button><span class="preview-arrow">＋</span></div>
      </div>
    </article>`).join("");
  emptyState.hidden = visible.length !== 0;
  grid.hidden = visible.length === 0;
  document.querySelectorAll("[data-preview]").forEach((button) => {
    button.addEventListener("click", () => openPreview(button.dataset.preview));
  });
}

function openPreview(id) {
  const item = products.find((product) => product.id === id);
  if (!item) return;
  document.getElementById("modal-category").textContent = item.label;
  document.getElementById("modal-title").textContent = item.name;
  document.getElementById("modal-description").textContent = item.description;
  document.getElementById("modal-art").innerHTML = `<span>${item.glyph}</span>`;
  modal.hidden = false;
}

document.querySelectorAll(".filter").forEach((button) => {
  button.addEventListener("click", () => {
    activeCategory = button.dataset.category;
    document.querySelectorAll(".filter").forEach((filter) => filter.classList.toggle("active", filter === button));
    renderProducts();
  });
});

document.querySelectorAll(".side-link").forEach((button) => {
  button.addEventListener("click", () => {
    activeView = button.dataset.view;
    document.querySelectorAll(".side-link").forEach((link) => link.classList.toggle("active", link === button));
    const title = document.getElementById("section-title");
    if (activeView === "collection") {
      title.textContent = "Your collection";
      grid.innerHTML = '<div class="empty-state collection-empty">Your owned cosmetics will appear here once account ownership is connected.</div>';
      grid.hidden = false;
      emptyState.hidden = true;
      document.getElementById("filter-row").hidden = true;
      searchInput.value = "";
    } else {
      title.textContent = "Featured cosmetics";
      document.getElementById("filter-row").hidden = false;
      activeCategory = "all";
      document.querySelectorAll(".filter").forEach((filter) => filter.classList.toggle("active", filter.dataset.category === "all"));
      renderProducts();
    }
  });
});

searchInput.addEventListener("input", () => {
  if (activeView !== "discover") {
    document.querySelector('[data-view="discover"]').click();
  }
  renderProducts();
});

document.getElementById("explore-button").addEventListener("click", () => {
  document.getElementById("catalogue").scrollIntoView({behavior:"smooth"});
});
document.getElementById("modal-close").addEventListener("click", () => { modal.hidden = true; });
modal.addEventListener("click", (event) => { if (event.target === modal) modal.hidden = true; });
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") modal.hidden = true;
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
    event.preventDefault();
    searchInput.focus();
  }
});
renderProducts();
