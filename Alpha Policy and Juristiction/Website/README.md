# Alpha Policy Website Prototype

This folder contains a standalone website prototype for the Alpha policy library. It follows the dark, gold-accented visual direction and standalone HTML/CSS/JavaScript structure used by the Alpha Store prototype.

## Included
- Responsive policy-library layout with sidebar categories.
- Search, category filters, and sorting.
- Policy cards with IDs, versions, descriptions, and draft labels.
- Full document reader that loads the Markdown source files from the sibling `../Policies/` folder.
- Clear draft warnings throughout the interface.
- No account, Supabase, payment, acceptance, enforcement, or Launcher integration.

## Displayed documents
The website lists user-facing policy drafts POL-001–POL-004 and POL-006–POL-011. POL-005 (Staff Code of Conduct) and POL-012 (Data Retention and Privacy Requests Procedure) are internal operational documents and are intentionally not exposed in this viewer.

## Preview locally
Because the reader loads the Markdown files from the sibling folder, serve the project over HTTP rather than opening `index.html` directly with a `file://` URL.

From the repository root, run a local static server, for example:

```bash
python -m http.server 8000
```

Then open:

`http://localhost:8000/Alpha%20Policy%20and%20Juristiction/Website/`

## Boundaries
- This is an internal prototype on the `mod-ideas` branch.
- The policy documents remain drafts and are not effective or approved for publication.
- This website is not wired into Alpha Launcher and does not publish or enforce policies.
- No GitHub Pages workflow is added for this website. The existing Store publishing workflow is left unchanged.
