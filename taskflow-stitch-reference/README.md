# TaskFlow — Google Stitch Reference Pack

This folder contains the four Google Stitch screen exports supplied by the project owner.

## Screens
- `screens/dashboard.html` and `screens/dashboard.png`
- `screens/my-tasks.html` and `screens/my-tasks.png`
- `screens/kanban-board.html` and `screens/kanban-board.png`
- `screens/task-detail-drawer.html` and `screens/task-detail-drawer.png`
- `DESIGN.md` — shared Stitch design tokens and typography guidance

## How to use
Treat these files as visual and interaction references for the React + TypeScript frontend.
Do not use the static HTML files as the final application architecture. Rebuild the screens as
reusable React components, preserve the visual design, and replace sample content with data
from the existing Spring Boot API.

The HTML exports reference Tailwind CSS and Google Fonts through external CDN URLs. Review
these dependencies and migrate the required styles/fonts into the frontend's own setup.

The screens contain illustrative sample content and UI elements that may not be supported by
the current backend (for example projects, team members, sprint metrics, and activity feeds).
Do not fabricate backend functionality or data for those items.

The same `DESIGN.md` was included in all four original ZIP files; it is stored once here.
