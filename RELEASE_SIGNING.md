# 本机签名发布

GitHub 仅编译未签名的安装包。正式 APK 下载到本机后，使用 `.private/niu-release.jks` 签名，再上传 GitHub Releases。私钥与密码不上传远端。

签名资料 `.private/niu-release.jks`、`.private/signing.json` 已加入忽略列表，后续更新必须复用，请单独安全备份。丢失签名资料将无法给已安装版本做覆盖升级。

APK 包名 `cn.weiping.niu`，最低 Android 8.0。JSON 是应用数据的迁移方式，网页和安卓存储独立；卸载前必须备份。
