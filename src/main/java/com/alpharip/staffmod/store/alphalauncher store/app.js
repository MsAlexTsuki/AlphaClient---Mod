const products = [
  {id:"aurora-cape",name:"Aurora Cape",category:"capes",label:"CAPE",description:"A clean gold-accented cape concept for the Alpha collection. Its understated design keeps the Alpha mark in focus.",glyph:"A",art:"cape",badge:"FEATURED",owned:true},
  {id:"ember-cape",name:"Ember Cape",category:"capes",label:"CAPE",description:"A warm, ember-inspired cape concept with a bold golden glow.",glyph:"✦",art:"cape",badge:"PREVIEW",owned:false},
  {id:"wave-emote",name:"Wave Emote",category:"emotes",label:"EMOTE",description:"A simple greeting emote concept for player expression.",glyph:"↗",art:"emote",badge:"PREVIEW",owned:false},
  {id:"alpha-tag",name:"Alpha Nameplate",category:"nametags",label:"NAMETAG",description:"A distinctive Alpha-styled nameplate treatment designed to make a player name stand out.",glyph:"ALPHA",art:"name",badge:"COMMUNITY",owned:true},
  {id:"spark-effect",name:"Golden Spark",category:"effects",label:"EFFECT",description:"A subtle gold particle-effect concept for a clean visual accent.",glyph:"✧",art:"effect",badge:"PREVIEW",owned:false},
  {id:"dark-ui",name:"Obsidian UI",category:"ui",label:"UI STYLE",description:"A dark interface accent concept for the Alpha experience.",glyph:"A",art:"ui",badge:"PREVIEW",owned:false}
];

const grid = document.getElementById("product-grid");
const searchInput = document.getElementById("search-input");
const emptyState = document.getElementById("empty-state");
const modal = document.getElementById("modal");
const filterRow = document.getElementById("filter-row");
const sortSelect = document.getElementById("sort-select");
const collectionNotice = document.getElementById("collection-notice");
const modalEquip = document.getElementById("modal-equip");
let activeCategory = "all";
let activeView = "discover";
let currentItemId = null;

const ownedItems = products.filter((item) => item.owned);
const collectionCount = document.getElementById("collection-count");
collectionCount.textContent = String(ownedItems.length);

function getEquippedId() {
  try {
    const saved = localStorage.getItem("alpha-store-demo-equipped");
    return ownedItems.some((item) => item.id === saved) ? saved : "aurora-cape";
  } catch {
    return "aurora-cape";
  }
}

let equippedId = getEquippedId();

function setEquippedId(id) {
  equippedId = equippedId === id ? null : id;
  try {
    if (equippedId) localStorage.setItem("alpha-store-demo-equipped", equippedId);
    else localStorage.removeItem("alpha-store-demo-equipped");
  } catch {
    // The prototype remains usable when browser storage is unavailable.
  }
  renderProducts();
  if (currentItemId) updateModalState(currentItemId);
}

function getVisibleProducts() {
  const query = searchInput.value.trim().toLowerCase();
  let visible = products.filter((item) => {
    const inView = activeView === "collection" ? item.owned : true;
    const categoryMatch = activeView === "collection" || activeCategory === "all" || item.category === activeCategory;
    const searchMatch = !query || [item.name, item.label, item.description].join(" ").toLowerCase().includes(query);
    return inView && categoryMatch && searchMatch;
  });

  if (sortSelect.value === "name") visible.sort((a, b) => a.name.localeCompare(b.name));
  if (sortSelect.value === "category") visible.sort((a, b) => a.label.localeCompare(b.label) || a.name.localeCompare(b.name));
  return visible;
}

function renderProducts() {
  const visible = getVisibleProducts();
  grid.innerHTML = visible.map((item) => {
    const isEquipped = equippedId === item.id;
    const ownedMarkup = item.owned ? '<span class="owned-pill">OWNED</span>' : "";
    const status = item.owned
      ? (isEquipped ? '<span class="card-status equipped">Equipped</span>' : '<span class="card-status">In collection</span>')
      : '<span class="card-status">Preview item</span>';
    return `
      <article class="product-card ${item.owned ? "owned" : ""}" data-item="${item.id}" tabindex="0" role="button" aria-label="View ${item.name} details">
        <div class="product-art" data-art="${item.art}">
          <span class="product-badge">${item.badge}</span>${ownedMarkup}
          <span class="product-glyph">${item.glyph}</span>
        </div>
        <div class="product-meta">
          <div class="product-category">${item.label}</div>
          <h3 class="product-name">${item.name}</h3>
          <p class="product-desc">${item.description}</p>
          <div class="card-bottom">${status}<span class="preview-link">Details ↗</span></div>
        </div>
      </article>`;
  }).join("");

  const noResults = visible.length === 0;
  emptyState.hidden = !noResults;
  grid.hidden = noResults;
  document.getElementById("empty-title").textContent = activeView === "collection" ? "Your collection is empty" : "No cosmetics found";
  document.getElementById("empty-description").textContent = activeView === "collection"
    ? "Cosmetics assigned to your account will appear here."
    : "Try another search or category.";
}

function updateModalState(id) {
  const item = products.find((product) => product.id === id);
  if (!item) return;
  const isEquipped = equippedId === item.id;
  document.getElementById("modal-status").textContent = item.owned
    ? (isEquipped ? "Equipped in demo" : "Owned in demo")
    : "Preview only";
  modalEquip.hidden = !item.owned;
  modalEquip.textContent = isEquipped ? "Unequip cosmetic" : "Equip cosmetic";
  modalEquip.classList.toggle("is-equipped", isEquipped);
  document.getElementById("modal-note").textContent = item.owned
    ? "Local demo state · Not synced to an account"
    : "Preview item · Not available for purchase";
}

function openDetails(id) {
  const item = products.find((product) => product.id === id);
  if (!item) return;
  currentItemId = id;
  document.getElementById("modal-category").textContent = item.label;
  document.getElementById("modal-title").textContent = item.name;
  document.getElementById("modal-description").textContent = item.description;
  document.getElementById("modal-art").dataset.art = item.art;
  document.getElementById("modal-art").querySelector("span").textContent = item.glyph;
  document.getElementById("modal-badge").textContent = item.badge;
  document.getElementById("modal-type").textContent = item.label;
  updateModalState(id);
  modal.hidden = false;
  document.getElementById("modal-close").focus();
}

function setView(view) {
  activeView = view;
  document.querySelectorAll(".side-link").forEach((link) => link.classList.toggle("active", link.dataset.view === view));
  const inCollection = view === "collection";
  document.getElementById("section-title").textContent = inCollection ? "My collection" : "Featured cosmetics";
  document.getElementById("section-eyebrow").textContent = inCollection ? "YOUR ALPHA ITEMS" : "FIND YOUR STYLE";
  filterRow.hidden = inCollection;
  collectionNotice.hidden = !inCollection;
  sortSelect.value = "featured";
  if (!inCollection) {
    activeCategory = "all";
    document.querySelectorAll(".filter").forEach((filter) => filter.classList.toggle("active", filter.dataset.category === "all"));
  }
  renderProducts();
}

document.querySelectorAll(".filter").forEach((button) => {
  button.addEventListener("click", () => {
    activeCategory = button.dataset.category;
    document.querySelectorAll(".filter").forEach((filter) => filter.classList.toggle("active", filter === button));
    renderProducts();
  });
});

document.querySelectorAll(".side-link").forEach((button) => {
  button.addEventListener("click", () => setView(button.dataset.view));
});

grid.addEventListener("click", (event) => {
  const card = event.target.closest("[data-item]");
  const previewButton = event.target.closest("[data-preview]");
  const id = previewButton ? previewButton.dataset.preview : card?.dataset.item;
  if (id) openDetails(id);
});

grid.addEventListener("keydown", (event) => {
  if ((event.key === "Enter" || event.key === " ") && event.target.matches("[data-item]")) {
    event.preventDefault();
    openDetails(event.target.dataset.item);
  }
});

searchInput.addEventListener("input", () => {
  if (activeView !== "discover") setView("discover");
  renderProducts();
});
sortSelect.addEventListener("change", renderProducts);

document.getElementById("clear-filters").addEventListener("click", () => {
  searchInput.value = "";
  activeCategory = "all";
  document.querySelectorAll(".filter").forEach((filter) => filter.classList.toggle("active", filter.dataset.category === "all"));
  if (activeView === "collection") setView("discover");
  else renderProducts();
});

document.getElementById("explore-button").addEventListener("click", () => {
  document.getElementById("catalogue").scrollIntoView({behavior:"smooth"});
  searchInput.focus({preventScroll:true});
});

modalEquip.addEventListener("click", () => {
  if (currentItemId) setEquippedId(currentItemId);
});
document.getElementById("modal-close").addEventListener("click", () => {
  modal.hidden = true;
  currentItemId = null;
});
modal.addEventListener("click", (event) => {
  if (event.target === modal) {
    modal.hidden = true;
    currentItemId = null;
  }
});
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && !modal.hidden) {
    modal.hidden = true;
    currentItemId = null;
  }
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
    event.preventDefault();
    searchInput.focus();
  }
});

renderProducts();
