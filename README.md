# gradle-transitive-version-lint-plugin

指定したライブラリのバージョンが、推移的依存によって意図せず変わっていないかを検証する Gradle プラグインです。
Android アプリケーションモジュール(`com.android.application`)向けです。

## 検知する内容

`verifyTransitiveDependencies` タスクは `*ReleaseRuntimeClasspath` と buildscript の `classpath` を対象に、
指定したライブラリごとに以下をチェックします。

| エラー | 内容 |
| --- | --- |
| `Transitive Dependencies Detected` | 最終的に解決されたバージョンが指定バージョンと異なる |
| `Multiple Versions Detected` | プロジェクト内のモジュールから異なるバージョンが直接宣言されている |
| `Library Not Used` | 依存グラフに存在しない、またはどのモジュールからも直接宣言されていない |

結果は `build/reports/transitive-dependencies/report.txt` に出力されます。
`ignoreFailures = true` にすると、エラーがあってもタスクを失敗させず、レポートの出力と警告のみ行います。

## サンプル

[`sample`](sample) はプラグインをソースから参照する Android プロジェクトです。

| モジュール | 内容 |
| --- | --- |
| `app-success` | 指定したバージョンのまま解決され、検証が成功する |
| `app-transitive-error` | ライブラリやプラグイン（AGP）の推移的依存でバージョンが上がり、`Transitive Dependencies Detected` を検知する |

以下で各モジュールの検証を実行し、期待するエラーがすべて検知されているかを確認します。CI でも実行しています。

```shell
cd sample
../gradlew verifySample
```
