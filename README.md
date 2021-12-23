# try-auth0-playframework

# 概要

Auth0で認証・認可するサンプルアプリケーションです。  
検証実装ですのでプロダクションでは使用しないでください。  

# auth0 getting started

Get Started : https://auth0.com/docs/get-started  

# 実装について

以下のドキュメントを参考に実装しました。  
Call Your API Using the Authorization Code Flow : https://auth0.com/docs/authorization/flows/call-your-api-using-the-authorization-code-flow  

取得したアクセストークンはセッションに保持してます。  
細かな実装についてはコード内にコメントで記述しています。  

# auth0の設定

auth0自体の設定はdocs以下に格納しています。  
application.confを作成し以下の項目を設定してください。  

- auth0のダッシュボードを確認してdomain, client.id, client.secretを設定してください。
- 必要に応じてcallback.url, logout.callback.urlも設定する。

以下は例です。  
```conf
auth0 {
  domain = "HOGEHOGEHOGEHOGEHO.auth0.com"
  client.id = "client-id-here"
  client.secret = "client-secret-here"
  callback.url = "http://localhost:9000/callback"
  logout.callback.url = "http://localhost:9000/"
}
```
