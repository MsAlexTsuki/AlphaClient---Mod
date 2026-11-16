const policies = [
  {id:"POL-001",title:"Terms of Service",file:"POL-001-Terms-of-Service.md",category:"Core",description:"The intended terms for accessing and using Alpha services.",version:"0.1-draft"},
  {id:"POL-002",title:"Privacy Policy",file:"POL-002-Privacy-Policy.md",category:"Privacy",description:"What information Alpha receives, why it is used, and how it is handled.",version:"0.3-draft"},
  {id:"POL-003",title:"Acceptable Use Policy",file:"POL-003-Acceptable-Use-Policy.md",category:"Safety",description:"Expected and prohibited use of Alpha software, accounts, and services.",version:"0.1-draft"},
  {id:"POL-004",title:"Community Guidelines",file:"POL-004-Community-Guidelines.md",category:"Community",description:"Standards for participation in Alpha community spaces.",version:"0.1-draft"},
  {id:"POL-006",title:"Ranks and Permissions Policy",file:"POL-006-Ranks-and-Permissions-Policy.md",category:"Access",description:"The intended rank structure, access boundaries, and permission principles.",version:"0.1-draft"},
  {id:"POL-007",title:"Store and Digital Content Policy",file:"POL-007-Store-and-Digital-Content-Policy.md",category:"Store",description:"Rules intended to apply to cosmetics, digital content, and any future sales.",version:"0.1-draft"},
  {id:"POL-008",title:"Beta Testing Policy",file:"POL-008-Beta-Testing-Policy.md",category:"Programs",description:"Expected terms for participation in Alpha beta testing.",version:"0.1-draft"},
  {id:"POL-009",title:"Security and Vulnerability Disclosure",file:"POL-009-Security-and-Vulnerability-Disclosure-Policy.md",category:"Safety",description:"How security issues should be reported and handled once a process is approved.",version:"0.1-draft"},
  {id:"POL-010",title:"Service Availability and Third-Party Services",file:"POL-010-Service-Availability-and-Third-Party-Services-Policy.md",category:"Services",description:"Service availability, maintenance, and external service dependencies.",version:"0.1-draft"},
  {id:"POL-011",title:"Cookie and Tracking Notice",file:"POL-011-Cookie-and-Tracking-Notice.md",category:"Privacy",description:"A draft notice covering local storage, cookies, and tracking technologies pending technical verification.",version:"0.1-draft"}
];

const grid = document.getElementById("policy-grid");
const searchInput = document.getElementById("search-input");
const filterRow = document.getElementById("filter-row");
const categoryNav = document.getElementById("category-nav");
const emptyState = document.getElementById("empty-state");
const reader = document.getElementById("reader");
let activeCategory = "All";
let activePolicy = null;

const categories = ["All", ...new Set(policies.map(policy => policy.category))];
document.getElementById("policy-count").textContent = String(policies.length).padStart(2,"0");

function buildFilters() {
  filterRow.innerHTML = categories.map(category => `<button class="filter ${category === activeCategory ? "active" : ""}" data-category="${escapeHtml(category)}">${escapeHtml(category)}</button>`).join("");
  categoryNav.innerHTML = categories.filter(category => category !== "All").map(category => `<button class="side-link category-link" data-category="${escapeHtml(category)}"><span class="side-icon">·</span>${escapeHtml(category)}<span class="category-count">${policies.filter(p => p.category === category).length}</span></button>`).join("");
}
function getVisiblePolicies() {
  const query = searchInput.value.trim().toLowerCase();
  let visible = policies.filter(policy => {
    const categoryMatch = activeCategory === "All" || policy.category === activeCategory;
    const searchMatch = !query || [policy.id,policy.title,policy.category,policy.description].join(" ").toLowerCase().includes(query);
    return categoryMatch && searchMatch;
  });
  if (document.getElementById("sort-select").value === "name") visible.sort((a,b)=>a.title.localeCompare(b.title));
  if (document.getElementById("sort-select").value === "category") visible.sort((a,b)=>a.category.localeCompare(b.category)||a.id.localeCompare(b.id));
  if (document.getElementById("sort-select").value === "number") visible.sort((a,b)=>Number(a.id.slice(4))-Number(b.id.slice(4)));
  return visible;
}
function renderPolicies() {
  const visible = getVisiblePolicies();
  grid.innerHTML = visible.map(policy => `
    <article class="policy-card" data-policy="${policy.id}" tabindex="0" role="button" aria-label="Open ${escapeHtml(policy.title)}">
      <div class="card-top"><span class="policy-id">${policy.id}</span><span class="draft-pill">DRAFT</span></div>
      <div class="card-symbol">§</div>
      <div class="policy-category">${escapeHtml(policy.category)}</div>
      <h3>${escapeHtml(policy.title)}</h3>
      <p>${escapeHtml(policy.description)}</p>
      <div class="card-bottom"><span>Version ${escapeHtml(policy.version)}</span><span class="open-link">Read document ↗</span></div>
    </article>`).join("");
  emptyState.hidden = visible.length !== 0;
  grid.hidden = visible.length === 0;
}
function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, char => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[char]));
}
function inlineMarkdown(text) {
  let value = escapeHtml(text);
  value = value.replace(/\*\*(.+?)\*\*/g,"<strong>$1</strong>");
  value = value.replace(/\*(.+?)\*/g,"<em>$1</em>");
  value = value.replace(/`([^`]+)`/g,"<code>$1</code>");
  value = value.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g,'<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');
  return value;
}
function renderMarkdown(markdown) {
  const lines = markdown.replace(/\r/g,"").split("\n");
  const output = [];
  let paragraph = [], listType = null, tableRows = [], quote = [];
  const flushParagraph = () => { if(paragraph.length){output.push("<p>"+inlineMarkdown(paragraph.join(" "))+"</p>");paragraph=[];} };
  const flushList = () => { if(listType){output.push(`</${listType}>`);listType=null;} };
  const flushQuote = () => { if(quote.length){output.push("<blockquote>"+quote.map(line=>"<p>"+inlineMarkdown(line)+"</p>").join("")+"</blockquote>");quote=[];} };
  const flushTable = () => {
    if(!tableRows.length)return;
    const rows = tableRows.filter(row=>!/^\s*\|?\s*:?-{3,}/.test(row));
    output.push("<div class=\"table-wrap\"><table>"+rows.map((row,index)=>{
      const cells=row.trim().replace(/^\||\|$/g,"").split("|").map(cell=>cell.trim());
      const tag=index===0?"th":"td";
      return "<tr>"+cells.map(cell=>`<${tag}>${inlineMarkdown(cell)}</${tag}>`).join("")+"</tr>";
    }).join("")+"</table></div>");
    tableRows=[];
  };
  for(const raw of lines){
    const line=raw.trim();
    if(!line){flushParagraph();flushList();flushQuote();flushTable();continue;}
    if(line.startsWith("|")){flushParagraph();flushList();flushQuote();tableRows.push(line);continue;}
    flushTable();
    if(line.startsWith(">")){flushParagraph();flushList();quote.push(line.replace(/^>\s?/,""));continue;}
    flushQuote();
    const heading=line.match(/^(#{1,4})\s+(.+)$/);
    if(heading){flushParagraph();flushList();const level=heading[1].length;output.push(`<h${level}>${inlineMarkdown(heading[2])}</h${level}>`);continue;}
    if(/^---+$/.test(line)){flushParagraph();flushList();output.push("<hr>");continue;}
    const unordered=line.match(/^[-*]\s+(.+)$/);
    const ordered=line.match(/^\d+\.\s+(.+)$/);
    if(unordered||ordered){flushParagraph();const type=unordered?"ul":"ol";if(listType!==type){flushList();listType=type;output.push(`<${type}>`);}output.push("<li>"+inlineMarkdown((unordered||ordered)[1])+"</li>");continue;}
    flushList();paragraph.push(line);
  }
  flushParagraph();flushList();flushQuote();flushTable();
  return output.join("");
}
async function openPolicy(id) {
  const policy = policies.find(item=>item.id===id);
  if(!policy)return;
  activePolicy=policy;
  document.getElementById("reader-number").textContent=policy.id;
  document.getElementById("reader-name").textContent=policy.title;
  document.getElementById("reader-meta").innerHTML=`<span>${escapeHtml(policy.category)}</span><span>Version ${escapeHtml(policy.version)}</span><span>Draft · Not effective</span>`;
  document.getElementById("reader-title").textContent=policy.title;
  document.getElementById("document-body").innerHTML='<div class="loading-state">Loading policy document…</div>';
  reader.hidden=false;
  document.body.classList.add("reader-open");
  history.replaceState(null,"", "#"+policy.id.toLowerCase());
  try {
    const response=await fetch("../Policies/"+encodeURIComponent(policy.file));
    if(!response.ok)throw new Error("HTTP "+response.status);
    const markdown=await response.text();
    document.getElementById("document-body").innerHTML=renderMarkdown(markdown);
    document.querySelector(".document-content").scrollTop=0;
  } catch(error) {
    document.getElementById("document-body").innerHTML='<div class="load-error"><strong>Could not load this policy file.</strong><p>Open this prototype through a local web server so the website can read the Markdown files in the sibling Policies folder.</p><code>'+escapeHtml(error.message)+'</code></div>';
  }
}
function closeReader() {
  reader.hidden=true;
  document.body.classList.remove("reader-open");
  history.replaceState(null,"",location.pathname);
  activePolicy=null;
}
buildFilters();
renderPolicies();
filterRow.addEventListener("click",event=>{
  const button=event.target.closest("[data-category]");
  if(!button)return;
  activeCategory=button.dataset.category;
  buildFilters();renderPolicies();
});
categoryNav.addEventListener("click",event=>{
  const button=event.target.closest("[data-category]");
  if(!button)return;
  activeCategory=button.dataset.category;
  buildFilters();renderPolicies();
  document.querySelectorAll(".side-link").forEach(link=>link.classList.toggle("active",link.dataset.category===activeCategory));
});
document.querySelector('[data-view="all"]').addEventListener("click",()=>{
  activeCategory="All";buildFilters();renderPolicies();
  document.querySelectorAll(".side-link").forEach(link=>link.classList.toggle("active",link.dataset.view==="all"));
});
grid.addEventListener("click",event=>{
  const card=event.target.closest("[data-policy]");
  if(card)openPolicy(card.dataset.policy);
});
grid.addEventListener("keydown",event=>{
  if((event.key==="Enter"||event.key===" ")&&event.target.matches("[data-policy]")){event.preventDefault();openPolicy(event.target.dataset.policy);}
});
searchInput.addEventListener("input",renderPolicies);
document.getElementById("sort-select").addEventListener("change",renderPolicies);
document.getElementById("clear-filters").addEventListener("click",()=>{
  searchInput.value="";activeCategory="All";buildFilters();renderPolicies();
});
document.getElementById("browse-button").addEventListener("click",()=>document.getElementById("catalogue").scrollIntoView({behavior:"smooth"}));
document.getElementById("reader-close").addEventListener("click",closeReader);
document.getElementById("reader-back").addEventListener("click",closeReader);
reader.addEventListener("click",event=>{if(event.target===reader)closeReader();});
document.addEventListener("keydown",event=>{
  if(event.key==="Escape"&&!reader.hidden)closeReader();
  if((event.metaKey||event.ctrlKey)&&event.key.toLowerCase()==="k"){event.preventDefault();searchInput.focus();}
});
const initialHash=location.hash.slice(1).toUpperCase();
if(initialHash&&policies.some(policy=>policy.id===initialHash))openPolicy(initialHash);
