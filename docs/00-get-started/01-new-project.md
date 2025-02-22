---
title: Create a project
---

Requirements:

 - Java 11 or higher
 - [Babashka](https://github.com/babashka/babashka#installation)

Run this command to create a new Biff project:

```bash
bb -e '(load-string (slurp "https://biffweb.com/new-project.clj"))'
```

This will create a minimal CRUD app which demonstrates most of Biff's features.
Run `bb dev` to start the app on `localhost:8080`. Whenever you save a file,
Biff will:

 - Evaluate any changed Clojure files (and any files which depend on them)
 - Regenerate static HTML and CSS files
 - Run tests

Open the `src/your/app/repl.clj` file for notes about REPL-driven development.
When you're ready to deploy, see [Production](/docs/reference/production/).
