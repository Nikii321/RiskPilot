# Карта проекта RiskPilot

Корневой пакет: `com.evedenta.riskpilot`. Один Kotlin-файл содержит один одноимённый верхнеуровневый класс, объект или enum. Каталоги `analytics`, `persistence` и `web` заменены явными слоями.

```text
riskpilot/
├── RiskPilotApplication.kt
├── controller/                     HTTP, маршруты и преобразование ввода
│   └── advice/                     общий обработчик ошибок
├── service/                        сценарии работы приложения
│   ├── analysis/                   единый расчёт СППР
│   ├── risk/                       управление реестром
│   ├── mitigation/                 настройка альтернативных мероприятий
│   ├── decision/                   сохранение и сравнение решений
│   ├── knowledge/                  доступ к предметным правилам
│   └── ml/                         обучение, версии и активация модели
├── repository/                     JDBC и сохранение данных
│   ├── risk/
│   ├── mitigation/
│   ├── decision/
│   └── ml/
├── domain/                         понятия и правила предметной области
│   ├── risk/                       Risk, RiskAssessment, RiskFeatures, RiskCategory
│   ├── mitigation/                 MitigationMeasure, DefaultMeasureFactory
│   ├── scenario/                   Scenario
│   └── knowledge/                  KnowledgeRule, RuleHit
├── model/                          контракт приложения и API
│   ├── request/                    входные DTO по функциям
│   ├── response/                   результаты по функциям
│   └── mapper/                     преобразования в DTO
├── ml/                             обучение и применение ML без Spring
│   ├── data/                       CSV, генератор, историческое наблюдение
│   ├── training/                   LogisticRegressionTrainer
│   ├── inference/                  LogisticRegressionModel
│   └── model/                      параметры, метрики и сохранённая модель
├── algorithm/                      расчётные методы СППР без HTTP и JDBC
│   ├── ahp/                        AhpCalculator
│   ├── optimization/               ResourceOptimizer
│   ├── scenario/                   ScenarioAnalyzer
│   └── knowledge/                  KnowledgeRuleEngine
├── config/                         начальные настройки и демонстрационные данные
└── exception/                      предметные исключения
```

## Где искать конкретную функцию

| Задача | Точка входа |
|---|---|
| Изменить REST-методы рисков | `controller/RiskController.kt` |
| Найти общий расчёт СППР | `service/analysis/AnalysisService.kt` |
| Изменить SQL реестра | `repository/risk/RiskRepository.kt` |
| Изменить признаки риска | `domain/risk/RiskFeatures.kt` |
| Изменить контракт входного риска | `model/request/risk/RiskRequest.kt` |
| Найти результат общего анализа | `model/response/analysis/AnalysisResponse.kt` |
| Изменить формулу AHP | `algorithm/ahp/AhpCalculator.kt` |
| Изменить выбор портфеля | `algorithm/optimization/ResourceOptimizer.kt` |
| Изменить сценарии и Monte Carlo | `algorithm/scenario/ScenarioAnalyzer.kt` |
| Изменить продукционные правила | `algorithm/knowledge/KnowledgeRuleEngine.kt` |
| Изменить начальные альтернативы мер | `domain/mitigation/DefaultMeasureFactory.kt` |
| Изменить начальный реестр | `config/DemoRiskData.kt` |
| Изменить стандартную матрицу AHP и сценарии | `config/DefaultAnalysisConfig.kt` |
| Изменить обучение ML | `ml/training/LogisticRegressionTrainer.kt` |
| Найти применение обученной модели | `ml/inference/LogisticRegressionModel.kt` |
| Изменить CSV или синтетическую историю | `ml/data/` |
| Изменить управление версиями модели | `service/ml/ModelService.kt` |
| Изменить сохранение моделей | `repository/ml/ModelRepository.kt` |
| Изменить сравнение решений | `service/decision/DecisionService.kt` |
| Изменить экран панели | `src/main/resources/static/app.js` |

Пути в таблице, кроме последнего, относительно `src/main/kotlin/com/evedenta/riskpilot/`.

## Направление зависимостей

HTTP-контроллеры вызывают сервисы. Сервисы координируют репозитории, ML и расчётные алгоритмы. Репозитории выполняют SQL и сериализацию; в них нет выбора стратегии, обучения или сравнения решений. `domain` не зависит от API, Spring и JDBC. `ml` использует свои типы, признаки предметной области и библиотеку Smile; Spring и JDBC туда не входят. DTO запросов и ответов отделены от domain; например, `RiskRequest` преобразуется в `RiskAssessment`.

`ModelService` управляет жизненным циклом, а `LogisticRegressionModel` вычисляет числовой прогноз — это разные роли. `ModelSnapshot` хранит обученные параметры и датасет; `ModelVersion` является экспортируемым DTO. `DefaultMeasureFactory` задаёт начальную предметную политику и не принадлежит SQL-репозиторию или оптимизатору.

## Три основных маршрута выполнения

- Анализ: `AnalysisController → AnalysisService → ModelService + репозитории + AhpCalculator + ResourceOptimizer + ScenarioAnalyzer + KnowledgeRuleEngine → AnalysisResponse`.
- Обучение: `ModelController → ModelService → HistoryCsvCodec → LogisticRegressionTrainer → ModelRepository`.
- Сравнение: `DecisionController → DecisionService → DecisionRepository → DecisionComparison`.

## Как структура защищена

`PackageArchitectureTest` проверяет совпадение пакетов с каталогами, один тип в одном файле и допустимые зависимости слоёв. Например, он запрещает `domain → controller`, `ml → repository` и `controller → repository`. Проверка входит в обычный `./gradlew test`.

Планирование: `scheduler/ml/ModelRetrainingScheduler.kt` → `service/ml/ModelRetrainingService.kt` → `ModelService`. Настройки: `config/ml/ModelRetrainingProperties.kt`. Очередь и журнал: `TrainingDatasetRepository` и `TrainingRunRepository`. Метрики вынесены в `ml/evaluation`, групповое разбиение — в `ml/data`.
