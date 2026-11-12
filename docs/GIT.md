# Работа с Git в RiskPilot

RiskPilot — отдельный Git-репозиторий. Его корень: `/Users/nikolajvereschagin/Desktop/RiskPilot`. У него собственная папка `.git`, отдельный индекс и коммиты. Remote `origin` указывает на `https://github.com/Nikii321/RiskPilot.git`. Проект находится на рабочем столе и не входит в репозиторий Travel-AI.

## Что означает индекс

- Рабочая папка — файлы на диске.
- Индекс (staging area) — изменения, подготовленные следующему коммиту командой `git add`.
- Коммит — сохранённая локальная версия.
- Push — отправка коммитов в GitHub.
- Индексация IntelliJ IDEA — анализ кода редактором; к индексу Git отношения не имеет.

## Что отправляется в GitHub

Исходники `src`, настройки Gradle и wrapper, Dockerfile, Compose, README, документация, учебные данные и экспорт демонстрационной модели. Локальные `.env`, `.idea`, `.gradle`, `.kotlin`, `build`, база в `data` и CSV из `training-data` игнорируются. `.env.example` — публикуемый образец, а `docs/synthetic-history.csv` — намеренно включённая синтетическая история. Пароль локальной БД из `.env` в коммит не входит.

## Проверка перед коммитом

```bash
cd /Users/nikolajvereschagin/Desktop/RiskPilot
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

Откройте папку RiskPilot отдельным проектом. В Settings → Version Control → Directory Mappings корнем Git должна быть папка RiskPilot. Изменения смотрите в Commit, корень репозитория проверяйте перед Commit и Push. Если IDEA предлагает добавить обнаруженный Git root, выберите папку RiskPilot на рабочем столе.
