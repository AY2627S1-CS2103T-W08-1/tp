---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how to set up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

**Building the website locally**

Run these commands from the repository root. They need Node.js, npm, Java and Graphviz: MarkBind uses Java and
Graphviz to draw the PlantUML diagrams.

1. `npm --prefix docs ci` installs the MarkBind version pinned in `docs/package-lock.json`.
1. `npm --prefix docs run build` builds the website into `docs/_site`.
1. `npm --prefix docs run serve` builds the website and opens a live preview that updates as you edit.

`docs/_site` is ignored by Git. Do not commit it.

CI (`.github/workflows/docs.yml`) builds and deploys the website with the newest MarkBind 7 (`^7.2.1`) on each push
to `master`. `npm` installs version 5.1.0, so check the deployed website after you change the page layout.

**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
