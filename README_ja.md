# IceBrew

[English README](./README.md) is also available.

**モダンWeb開発のためのSpring Boot + Vite統合フレームワーク**

IceBrewは、ViteデベロッパーサーバーとSpring Bootをシームレスに統合するオープンソースフレームワークです。開発モードでHot Module Replacement (HMR)による優れた開発体験を提供し、プロダクションモードでは効率的な静的ファイル配信を実現します。

##  機能

-  **Hot Module Replacement (HMR)** - 開発中の即座のフィードバック
-  **自動プロキシ** - Vite dev serverへの静的アセットの自動プロキシ
-  **ゼロコンフィグ** - 適切なデフォルト設定で即座に動作
-  **プロダクション対応** - ビルド出力からの自動静的ファイル配信
-  **CLIツール** - 複数のフレームワークオプションで素早くプロジェクトをスキャフォールド
-  **マルチフレームワーク対応** - React、Vue、Svelte、バニラJavaScript
-  **Vite駆動** - 超高速ビルドと開発体験
-  **Spring Boot 3** - 最新のSpring Bootプラットフォーム上に構築

## 主要機能

### 開発モード
- Vite dev serverがJavaバックエンドによって自動的に起動されます
- 静的ファイルリクエストがVite dev serverにプロキシされます
- HMRがシームレスに動作します
- ホットアップデート用のWebSocket接続が適切に処理されます

### プロダクションモード
- ビルドされたフロントエンドアセット(`dist/`)がSpring Bootによって配信されます
- 効率的な静的リソース処理
- プロダクションデプロイメントに最適化

##  インストール

### Maven

`pom.xml`にIceBrewスターターを追加:

```xml
<dependency>
    <groupId>io.icebrew</groupId>
    <artifactId>icebrew-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

##  クイックスタート

### CLIを使用

新しいIceBrewプロジェクトを作成する最も簡単な方法:

```bash
# CLIをビルド
cd icebrew-cli
mvn clean package

# 新しいプロジェクトを作成
java -jar target/icebrew-cli-0.1.0-SNAPSHOT-jar-with-dependencies.jar create my-app

# 開発を開始
cd my-app
cd frontend && npm install && cd ..
mvn spring-boot:run
```

アプリケーションは`http://localhost:8080`で利用可能になります

### 手動セットアップ

1. **Spring Bootプロジェクトを作成** し、IceBrewスターター依存関係を追加

2. **`application.properties`を設定**:

```properties
# サーバー設定
server.port=8080

# IceBrew Vite設定
icebrew.vite.enabled=true
icebrew.vite.host=localhost
icebrew.vite.port=5173
icebrew.vite.frontend-dir=frontend
icebrew.vite.build-dir=dist
icebrew.vite.auto-start=true
```

3. **`frontend/`ディレクトリにフロントエンドを作成**:

```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
```

4. **アプリケーションを実行**:

```bash
mvn spring-boot:run
```

##  設定オプション

| プロパティ | デフォルト | 説明 |
|----------|---------|-------------|
| `icebrew.vite.enabled` | `true` | Vite統合の有効化/無効化 |
| `icebrew.vite.host` | `localhost` | Vite dev serverのホスト |
| `icebrew.vite.port` | `5173` | Vite dev serverのポート |
| `icebrew.vite.frontend-dir` | `frontend` | フロントエンドソースディレクトリ |
| `icebrew.vite.build-dir` | `dist` | ビルド出力ディレクトリ |
| `icebrew.vite.auto-start` | `true` | Vite dev serverの自動起動 |
| `icebrew.vite.base-path` | `""` | ViteのベースURLパス |
| `icebrew.vite.startup-timeout` | `60` | 起動タイムアウト（秒） |

##  プロジェクト構造

```
my-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── DemoApplication.java
│       │       └── ApiController.java
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   └── index.css
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
└── pom.xml
```

##  高度な使用方法

### カスタムAPIエンドポイント

Spring BootアプリケーションでRESTコントローラーを作成:

```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }
}
```

フロントエンドからアクセス:

```typescript
fetch('/api/hello')
  .then(res => res.json())
  .then(data => console.log(data.message))
```

### プロダクションビルド

1. フロントエンドをビルド:
```bash
cd frontend
npm run build
```

2. Spring Bootをプロダクションモードで実行:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

またはJARとしてパッケージ化:
```bash
mvn clean package
java -jar target/my-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

##  対応フロントエンドフレームワーク

- **React** - React 18 with TypeScript
- **Vue** - Vue 3 with TypeScript
- **Svelte** - Svelte with TypeScript
- **Vanilla** - プレーンJavaScript/TypeScript

今後さらに多くのフレームワークに対応予定！

## コントリビュート

コントリビューションを歓迎します！お気軽にPull Requestを送ってください。

1. リポジトリをフォーク
2. フィーチャーブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add some amazing feature'`)
4. ブランチにプッシュ (`git push origin feature/amazing-feature`)
5. Pull Requestを開く

##  ライセンス

このプロジェクトはApache License 2.0の下でライセンスされています。詳細は[LICENSE](LICENSE)ファイルをご覧ください。

##  謝辞
- [Spring Boot](https://spring.io/projects/spring-boot) - 素晴らしいフレームワークを提供してくれたSpringチーム
- [Vite](https://vitejs.dev/) - 超高速ビルドツールを作ってくれたEvan YouとViteチーム
- このプロジェクトをより良くしてくれた全てのコントリビューター

---
