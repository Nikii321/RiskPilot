# Работа с Git в RiskPilot

RiskPilot — отдельный Git-репозиторий. Его корень: `/Users/nikolajvereschagin/Travel-AI/RiskPilot`. У него собственная папка `.git`, отдельный индекс, коммиты и remote `origin`. Расположение внутри каталога Travel-AI не объединяет их истории. В локальном `.git/info/exclude` родительского репозитория указан `/RiskPilot/`, поэтому родитель не предлагает добавить этот проект.

## Что означает индекс

- Рабочая папка — файлы на диске.
- Индекс (staging area) — изменения, подготовленные следующему коммиту командой `git add`.
- Коммит — сохранённая локальная версия.
- Push — отправка коммитов в GitHub.
- Индексация IntelliJ IDEA — анализ кода редактором; к индексу Git отношения не имеет.

## Что отправляется в GitHub

Исходники `src`, настройки Gradle и wrapper, Dockerfile, Compose, GitHub Actions, README, документация, учебные данные и экспорт демонстрационной модели. Локальные `.env`, `.idea`, `.gradle`, `.kotlin`, `build`, база в `data` и CSV из `training-data` игнорируются. `.env.example` — публикуемый образец, а `docs/synthetic-history.csv` — намеренно включённая синтетическая история. Пароль локальной БД из `.env` в коммит не входит.

## Проверка перед коммитом

```bash
cd /Users/nikolajvereschagin/Travel-AI/RiskPilot
git rev-parse --show-toplevel
git status --short
git diff
git diff --cached --stat
git diff --cached
```

В `git status --short` первый столбец показывает индекс, второй — изменения рабочей папки. `??` означает новый файл вне индекса; `A ` — новый подготовленный файл; ` M` — изменённый файл вне индекса; `M ` — подготовленное изменение. После успешного коммита и push `git status` должен сообщать о чистой рабочей папке и синхронизации с `origin/main`.

## Обычная работа

```bash
git add src README.md docs
git diff --cached --stat
git commit -m "Describe the change"
git push
```

Чтобы убрать файл из индекса, сохранив его на диске, используйте `git restore --staged путь/к/файлу`.

## IntelliJ IDEA

Откройте папку RiskPilot отдельным проектом. В Settings → Version Control → Directory Mappings корнем Git должна быть папка RiskPilot. Изменения смотрите в Commit, корень репозитория проверяйте перед Commit и Push. После создания вложенного репозитория IDEA может предложить добавить обнаруженный Git root — выберите RiskPilot. Перемещать проект на диске не требуется.
