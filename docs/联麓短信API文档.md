# 联麓信息 短信 API 3.0 文档

> 接口基础地址: `https://apis.shlianlu.com`

## 账户凭证

| 凭证 | 值 |
|------|----|
| MchId（企业ID） | `1066919` |
| AppId（通知短信） | `10011752649944698` |
| AppKey（通知短信密钥） | `f09e50fc096f48f3a16e39811f1ee716` |
| AppId（营销短信） | `10021752649944783` |
| AppKey（营销短信密钥） | `c467e1efac264b57897678485d8b1003` |

---

## 1. 签名创建接口

### 1.1 概览

签名创建, 短信发送必带签名。

### 1.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/sign/create
```

### 1.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 1.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| content | String | 是 | 短信签名内容，不能包含表情和符号 |
| Signature | String | 是 | 数字签名，详见签名方法 |
| type | int | 是 | 签名来源：1=本公司（签名与本账户认证主体有关联），2=他公司（签名与本公司认证主体无关联） |
| label | int | 是 | 1=营业执照，2=商标，3=APP，默认值为 1 |
| creditCodeUrl | String | 否 | 营业执照，媒体文件url链接，type=2 时必填 |
| idCardFront | String | 否 | 法人身份证正面图片url链接，type=2 时必填 |
| idCardBack | String | 否 | 法人身份证反面图片url链接，type=2 时必填 |
| sxCommits | String | 是 | 用户接收意愿承诺函 url链接 |
| screenshot | String | 否 | 商标/APP备案网查询截图url链接，label=2 或 3 时必填 |
| shopScreenshot | String | 否 | 应用商店截图url链接，label=3 时必填 |
| apphomeImg | String | 否 | APP首页截图，label=3 时必填 |
| fiveg | String | 否 | 5G授权书url链接，label=2或3 且 type=2 时必填 |
| contract | String | 否 | 签名公司的授权书，type=2 时必填 |
| handheld | String | 否 | 手持身份证照 url链接 |
| company | String | 否 | 公司名称，type=2 时必填 |
| legalPerson | String | 否 | 法人姓名，type=2 时必填 |
| creditCode | String | 否 | 信用代码，type=2 时必填 |
| creditUserName | String | 否 | 经办人姓名，type=2 时必填 |
| idCard | String | 否 | 经办人身份证号码，type=2 时必填 |
| phone | String | 否 | 经办人手机号，type=2 时必填 |

### 1.5 Signature 签名机制

签名串拼接方式:

```
AppId=XXX&MchId=XXX&SignType=MD5&TimeStamp=XXX&Version=1.2.0&content=XXX&creditCodeUrl=XXX&idCardBack=XXX&idCardFront=XXX&remark=XXX&type=XXX&key=XXX
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 1.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| SignId | int | 签名id，用于创建模版 |
| message | string | 请求具体回执 |
| timestamp | string | 时间戳 |
| status | string | 请求状态码 |

### 1.7 请求示例

```json
{
    "AppId": "100***6836",
    "MchId": "10***79",
    "SignType": "MD5",
    "TimeStamp": "1717397980",
    "Version": "1.2.0",
    "content": "上海联麓",
    "Signature": "1596A5859FFD49EDC68182BDB6697B5C",
    "type": "2",
    "label": "1",
    "company": "上海******有限公司",
    "legalPerson": "某**",
    "creditCode": "91***************",
    "creditUserName": "某**",
    "idCard": "123456**********41313",
    "phone": "150****1234",
    "creditCodeUrl": "https://www.shlianlu.com/assist/getImage/a161fd1765b9b59fbdcd278d94cd7943",
    "idCardFront": "https://www.shlianlu.com/assist/getImage/a161fd1765b9b59fbdcd278d94cd7943",
    "idCardBack": "https://www.shlianlu.com/assist/getImage/a161fd1765b9b59fbdcd278d94cd7943",
    "screenshot": "https://www.shlianlu.com/assist/getImage/a161fd1765b9b59fbdcd278d94cd7943",
    "fiveg": "https://www.shlianlu.com/assist/getImage/a161fd1765b9b59fbdcd278d94cd7943",
    "contract": "https://www.shlianlu.com/assist/getImage/a161fd1765b9b59fbdcd278d94cd7943"
}
```

### 1.8 返回示例

```json
{
    "SignId": 3387,
    "message": "success",
    "timestamp": 1652845247574,
    "status": "00"
}
```

---

## 2. 签名查询接口

### 2.1 概览

查询用户创建的签名列表。查单独签名可加参数 `SignId: "123"` 或 `SignName: "【联麓信息】"`（需要带上【】符号）。

### 2.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/sign/get
```

### 2.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 2.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |
| SignId | String | 否 | 签名id，查单个签名时传入 |
| SignName | String | 否 | 签名名称，查单个签名时传入（需带【】符号） |

### 2.5 Signature 签名机制

签名串拼接方式（所有字段排序拼接，最后加 `&key=Key`）:

```
AppId=xxx&MchId=xxx&SignType=MD5&TimeStamp=1650784003&Version=1.2.0&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 2.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| data | Array | 签名列表 |
| data[].signId | int | 签名id |
| data[].userId | int | 用户id |
| data[].userName | string | 用户名 |
| data[].productId | int | 产品id |
| data[].signName | string | 签名名称 |
| data[].content | string | 签名内容（含【】） |
| data[].status | int | 审核状态：1=审核通过，2=待审核，3=审核不通过，4=报备中 |
| data[].ctime | string | 创建时间 |
| data[].signFlag | int | 签名标识 |
| data[].signPostCount | int | - |
| data[].signSendCount | int | - |
| data[].isDefault | int | 是否默认签名 |
| data[].refuseReason | string | 拒绝原因 |
| data[].extCode | string | 扩展码 |
| data[].remark | string | 备注 |
| data[].ext1 | string | 扩展字段1 |
| message | string | 请求具体回执 |
| timestamp | string | 时间戳 |
| status | string | 请求状态码 |

### 2.7 请求示例

```json
{
    "AppId": "10*************00",
    "Version": "1.2.0",
    "MchId": "100***66",
    "Signature": "2330B2D29548C8014E24E7200B479496",
    "SignType": "MD5",
    "TimeStamp": "1652845075270"
}
```

### 2.8 返回示例

```json
{
    "data": [
        {
            "signId": 3693,
            "userId": 1001808,
            "userName": "15601862749",
            "productId": 0,
            "signName": "",
            "content": "【联麓信息】",
            "status": 1,
            "ctime": "2022-05-11T03:00:46.000+00:00",
            "signFlag": 0,
            "signPostCount": null,
            "signSendCount": null,
            "isDefault": 0,
            "refuseReason": "签名不规范，如有疑问请联系商务",
            "extCode": null,
            "remark": null,
            "ext1": null
        }
    ],
    "message": "success",
    "timestamp": 1652841633259,
    "status": "00"
}
```

### 2.9 错误码

参阅 API 错误代码。

---

## 3. 删除短信签名

### 3.1 概览

删除短信签名。

### 3.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/sign/delete
```

### 3.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 3.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| AppId | String | 是 | 短信 SdkAppId，在短信控制台添加应用后生成 |
| MchId | String | 是 | 企业ID |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| SignId | String | 是 | 短信签名ID |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 3.5 Signature 签名机制

签名串拼接方式:

```
AppId=xxx&MchId=xxx&SignId=xxx&SignType=MD5&TimeStamp=xxx&Version=1.2.0&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 3.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| message | string | 接口请求状态信息 |
| timestamp | string | 时间戳 |
| status | string | 接口请求状态码 |

### 3.7 请求示例

```json
{
    "MchId": "100***66",
    "AppId": "10*************00",
    "Version": "1.2.0",
    "TimeStamp": "1647485425382",
    "SignId": 13410,
    "SignType": "MD5",
    "Signature": "0834F17B4C445571A3C6D2FE8F827EAD"
}
```

### 3.8 返回示例

```json
{
    "message": "xxx已删除",
    "timestamp": 1652854398223,
    "status": "00"
}
```

---

## 4. 签名状态推送（回调）

### 4.1 概览

签名审核状态回调通知。配置地址在：相应产品页面 => 系统管理 => 产品配置 => 推送设置 => 签名状态推送地址。

### 4.2 请求URL

由用户在控制台配置回调地址。

### 4.3 请求方式

```
POST
```

### 4.4 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 4.5 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| productId | String | 产品类型 |
| status | String | 审核状态：1=通过，3=驳回 |
| id | String | 签名ID |
| title | String | 固定值：`签名审核` |
| content | String | 推送信息，格式：`您的签名：{id}审核成功/审核失败` |
| type | String | 固定值：`sign` |
| cTime | String | 创建时间 |
| refuseReason | String | 审核拒绝原因描述 |

### 4.6 请求示例

```json
{
    "productId": "1001",
    "status": "1",
    "id": "1768",
    "title": "签名审核",
    "content": "您的签名：1768审核成功",
    "type": "sign",
    "cTime": "1681868157273",
    "refuseReason": ""
}
```

### 4.7 错误码

参阅 API 错误代码。

---

## 5. API 状态码

| 状态码 | 说明 | 备注 |
|--------|------|------|
| 00 | success | 接口调用成功 |
| 01 | 参数有误 | 一般为缺少参数，或参数类型错误。例如 Type 为 String，传参时传了 Number |
| 02 | 数字签名错误 | 请比对签名字符串是否正确，签名的参数名和传参的大小写要保持一致 |
| 03 | 企业ID有误 | 请登录客户端查询企业ID（MchId） |
| 04 | 余额不足 | 请保持账户余额充足，如余额不足以完成本次请求，请求将不予处理 |
| 05 | 号码格式有误 | 参考 PhoneNumberSet 示例，PhoneNumberSet 不能为空 |
| 07 | 号码数量超限 | - |
| 08 | 请求频率限制 | - |
| 09 | 服务器内部错误 | - |
| 10 | 签名过长 | - |
| 11 | taskid不存在 | - |
| 12 | tag参数大于32位 | - |
| 13 | 账户不存在或已停用 | - |
| 14 | 待审核 | - |
| 15 | 驳回 | - |
| 16 | 无匹配模板 | - |
| 17 | 未发送 | - |
| 18 | 模板内容不能超过600字 | - |
| 19 | 短信字数超额 | - |
| 20 | 签名不存在，请先添加签名 | - |
| 21 | 价格计算错误 | - |
| 22 | 模板有误或没有审核 | - |
| 23 | 时间戳差异过大 | 与系统时间差异超过5分钟 |
| 24 | 签名ID不存在 | - |
| 25 | 签名已存在 | - |
| 26 | 关键字失败 | - |
| 27 | 账户无归属关系 | - |
| 28 | 限制发送时间 | - |
| 31 | AppId错误 | - |
| 38 | 签名参数错误 | - |
| 39 | 签名格式错误 | - |
| 40 | 非法的IP地址 | - |
| 41 | 产品未开通 | - |
| 42 | 账号无配置 | - |
| 43 | 未配置效验通道 | - |
| 44 | 身份证号格式错误 | - |
| 45 | 银行卡号格式错误 | - |
| 46 | 账号匹配失败 | - |

---

## 6. 创建短信模板

### 6.1 概览

创建短信模板。

### 6.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/template/create
```

### 6.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 6.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| SignId | int | 是 | 签名ID |
| content | String | 是 | 模板内容（无需携带签名）。营销短信模板格式：内容 + 退订文案，如"拒收请回复R"。支持变量格式 `{%变量1%}` |
| TemplateName | String | 是 | 短信模板名称 |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 6.5 Signature 签名机制

签名串拼接方式（所有字段排序拼接，最后加 `&key=Key`）:

```
AppId=xxx&MchId=xxx&SignId=xxx&SignType=MD5&TemplateName=xxx&TimeStamp=1650784003&Version=XXX&content=xxx&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 6.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| TemplateId | int | 模板id，用于发送模板短信 |
| message | String | 返回信息 |
| timestamp | int | 时间戳 |
| status | String | 状态码 |

### 6.7 请求示例

```json
{
    "SignId": 1341,
    "AppId": "100************77",
    "Version": "1.2.0",
    "MchId": "100**08",
    "Signature": "639B1665B7296DEBBDCC4198CE022E9A",
    "TemplateName": "测试",
    "SignType": "MD5",
    "TimeStamp": "1652854397094",
    "content": "您正在验证，验证码{%变量1%}，切勿将验证码泄露于他人，本条验证码有效期15分钟。"
}
```

### 6.8 返回示例

```json
{
    "message": "success",
    "TemplateId": 70001681,
    "timestamp": 1652854398223,
    "status": "00"
}
```

### 6.9 错误码

参阅 API 错误代码。

---

## 7. 模板短信发送

### 7.1 概览

模板短信发送接口，用于下发带变量的内容，如验证码、预警通知。

### 7.2 请求URL

```
POST https://apis.shlianlu.com/sms/trade/template/send
```

### 7.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 7.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业ID，登录联麓客户端点击（通知/营销短信）进入概览页面获取 |
| AppId | String | 是 | Appid，登录联麓客户端点击（通知/营销短信）进入概览页面获取 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| Type | String | 是 | 短信类型，本接口固定值 `3` |
| PhoneNumberSet | Array of String | 是 | 接收短信的手机号码数组，上限为 10000 |
| TemplateId | String | 是 | 模版id，登录联麓客户端进入短信模板页面创建模板获取 |
| TemplateParamSet | Array of String | 是 | 模板变量内容数组，根据模板变量数提交相应变量。不带变量时使用 `[]`，单个变量长度限制不超过20个字符 |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |
| TaskTime | String | 否 | 定时短信，设置短信按照预定时间发送，13位UNIX时间戳 |
| Tag | string | 否 | 自定义标签，拉取/推送报告时也会携带此参数 |
| CallUrl | String | 否 | 开启回执推送后，用于指定推送地址，未指定则推送至默认配置地址 |

### 7.5 Signature 签名机制

签名串拼接方式:

```
AppId=xxx&MchId=xxx&SignType=MD5&TemplateId=xxx&TimeStamp=xxx&Type=3&Version=XXX&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 7.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| status | string | 接口请求状态码 |
| taskId | string | 唯一请求 ID，每次请求都会返回，定位问题时需要提供 |
| timestamp | string | UNIX 时间戳 |
| message | string | 返回信息 |
| count | int | 发送批次预扣费总条数 |

### 7.7 请求示例

```json
{
    "Type": "3",
    "TemplateParamSet": ["111", "222", "222"],
    "PhoneNumberSet": ["15587571285", "15587571285"],
    "AppId": "10*************00",
    "Version": "***",
    "MchId": "100***66",
    "Signature": "15A8A0E43091A9B5C64CFAB50363C8CA",
    "SignType": "MD5",
    "TimeStamp": "1647483066580",
    "TemplateId": "70000278"
}
```

### 7.8 返回示例

```json
{
    "taskId": "202203170025179000005",
    "message": "success",
    "timestamp": 1647483044178,
    "status": "00",
    "count": 0
}
```

### 7.9 错误码

参阅 API 错误代码。

---

## 8. 模板个性短信发送

### 8.1 概览

个性短信发送接口，适用于给不同手机下发不同内容。

### 8.2 请求URL

```
POST https://apis.shlianlu.com/sms/trade/personal/send
```

### 8.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 8.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业ID，登录联麓客户端点击（通知/营销短信）进入概览页面获取 |
| AppId | String | 是 | Appid，登录联麓客户端点击（通知/营销短信）进入概览页面获取 |
| Version | String | 是 | 公共参数，取值：`1.2.0` |
| Type | String | 是 | 短信类型，本接口固定值 `2` |
| TemplateId | String | 是 | 模板ID |
| ContextParamSet | Array of Array | 是 | 号码和变量元素数组。格式：`[["手机号","变量1","变量2"], ...]`，号码放在变量之前，单个变量长度限制不超过20个字符 |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |
| TaskTime | String | 否 | 定时短信，设置短信按照预定的时间发送，UNIX时间戳 |
| Tag | string | 否 | 自定义标签，拉取/推送报告时也会携带此参数 |
| CallUrl | String | 否 | 开启回执推送后，用于指定推送地址，未指定则推送至默认配置地址 |

### 8.5 Signature 签名机制

签名串拼接方式:

```
AppId=xxx&MchId=xxx&SignType=MD5&TemplateId=xxx&TimeStamp=xxx&Type=2&Version=XXX&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 8.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| status | string | 接口请求状态码 |
| taskId | string | 唯一请求 ID，每次请求都会返回，定位问题时需要提供 |
| timestamp | string | UNIX 时间戳 |
| message | string | 返回信息 |
| count | int | 发送批次预扣费总条数 |

### 8.7 请求示例

```json
{
    "MchId": "100***66",
    "AppId": "10*************00",
    "Version": "1.2.0",
    "TimeStamp": "1651819248527",
    "SignType": "MD5",
    "Type": "2",
    "TemplateId": "12318***",
    "Signature": "E65938D99F2F36E0024ECF036656FF46",
    "ContextParamSet": [
        ["13062651230", "变量内容1", "变量内容2"],
        ["13062651231", "变量内容1", "变量内容2"]
    ]
}
```

### 8.8 返回示例

```json
{
    "taskId": "202203170025339000003",
    "message": "success",
    "timestamp": 1647482916338,
    "status": "00",
    "count": 0
}
```

### 8.9 错误码

参阅 API 错误代码。

---

## 9. 短信模板查询

### 9.1 概览

查询用户创建的短信模板。

### 9.2 请求URL

```
POST https://api.shlianlu.com/sms/product/template/getById
```

### 9.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 9.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| TemplateId | Int | 是 | 模板id |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 9.5 Signature 签名机制

签名串拼接方式（所有字段排序拼接，最后加 `&key=Key`）:

```
AppId=xxx&MchId=xxx&SignType=MD5&TemplateId=xxx&TimeStamp=xxx&Version=1.2.0&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 9.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| data.id | int | 模板ID |
| data.signId | int | 短信签名ID |
| data.signContent | string | 短信签名内容 |
| data.suffixSignId | int | 后缀签名ID |
| data.suffixSign | string | 后缀信息 |
| data.productId | int | 产品ID |
| data.templateName | string | 模板名称 |
| data.content | string | 模板内容 |
| data.status | int | 审核状态：1=审核通过，2=待审核，3=审核驳回 |
| data.ctime | string | 创建时间 |
| data.userId | int | 用户ID |
| data.userName | string | 用户名 |
| data.refuseReason | string | 拒绝原因 |
| data.postCount | int | 使用次数 |
| data.sendCount | int | 发送量（条） |
| message | string | 返回信息 |
| timestamp | int | 时间戳 |
| status | string | 状态码 |

### 9.7 请求示例

```json
{
    "MchId": "10***55",
    "AppId": "10*************94",
    "Version": "1.2.0",
    "SignType": "MD5",
    "Timestamp": "1672295345455",
    "TemplateId": 70003549,
    "Signature": "F131BAFED46DF14F3FD1762DEAB2E696"
}
```

### 9.8 返回示例

```json
{
    "data": {
        "user": null,
        "upmsSign": null,
        "id": 70028082,
        "signId": 71597,
        "signContent": "【联麓信息测试】",
        "suffixSignId": -1,
        "suffixSign": "",
        "productId": 1001,
        "templateName": "test",
        "content": "嘿，{%name%}您好，您的测试账户是{%id%}",
        "status": 1,
        "ctime": "2023-07-13T08:11:35.000+00:00",
        "userId": 1027779,
        "userName": "QP",
        "refuseReason": "",
        "channelCmc": null,
        "channelCuc": null,
        "channelCtc": null,
        "access": null,
        "type": null,
        "remark": null,
        "limitDay": null,
        "postCount": 9,
        "sendCount": 9
    },
    "message": "success",
    "timestamp": 1702975298240,
    "status": "00"
}
```

### 9.9 错误码

参阅 API 错误代码。

---

## 10. 编辑短信模板

### 10.1 概览

根据 TemplateId 修改模板名称（TemplateName）、短信签名（SignId）、模板内容（content）。修改什么内容就在请求中添加对应的参数名和参数值。

### 10.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/template/update
```

### 10.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 10.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| TemplateId | string | 是 | 短信模板id |
| TemplateName | string | 否 | 短信模板名称 |
| SignId | string | 否 | 短信签名id |
| content | String | 否 | 模板内容（无需携带签名），营销短信需加退订文案 |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 10.5 Signature 签名机制

签名串拼接方式（根据传入的参数拼接，参数为空不拼接）:

```
AppId=XXX&MchId=XXX&SignId=XXX&SignType=MD5&TemplateId=XXX&TemplateName=XXX&TimeStamp=XXX&Version=1.2.0&content=XXX&key=XXX
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 10.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| message | String | 返回信息 |
| timestamp | int | 时间戳 |
| status | String | 状态码 |

### 10.7 请求示例

```json
{
    "AppId": "10********0",
    "MchId": "10***79",
    "SignId": "73045",
    "SignType": "MD5",
    "TemplateId": "70028132",
    "TemplateName": "模板修改",
    "TimeStamp": "1689314199843",
    "Version": "1.2.0",
    "content": "修改模板测试20230722",
    "Signature": "639B1665B7296DEBBDCC4198CE022E9A"
}
```

### 10.8 返回示例

```json
{
    "message": "xxx已更新，请等待审核",
    "timestamp": 1652854398223,
    "status": "00"
}
```

### 10.9 错误码

参阅 API 错误代码。

---

## 11. 删除短信模板

### 11.1 概览

删除短信模板。

### 11.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/template/delete
```

### 11.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 11.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| TemplateId | string | 是 | 短信模板id |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 11.5 Signature 签名机制

签名串拼接方式:

```
AppId=xxx&MchId=xxx&SignType=MD5&TemplateId=xxx&TimeStamp=xxx&Version=1.2.0&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 11.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| message | String | 返回信息 |
| timestamp | int | 时间戳 |
| status | String | 状态码 |

### 11.7 请求示例

```json
{
    "MchId": "100**08",
    "AppId": "100************77",
    "Version": "1.2.0",
    "SignType": "MD5",
    "TimeStamp": "1652854397094",
    "TemplateId": 1341,
    "Signature": "639B1665B7296DEBBDCC4198CE022E9A"
}
```

### 11.8 返回示例

```json
{
    "message": "success",
    "timestamp": 1652854398223,
    "status": "00"
}
```

### 11.9 错误码

参阅 API 错误代码。

---

## 12. 短信模板列表查询

### 12.1 概览

查询用户创建的短信模板列表。

### 12.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/template/get
```

### 12.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 12.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 12.5 Signature 签名机制

签名串拼接方式:

```
AppId=xxx&MchId=xxx&SignType=MD5&TimeStamp=1650784003&Version=1.2.0&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 12.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| data | Array | 模板列表 |
| data[].id | int | 模板id，用于发送模板短信 |
| data[].signId | int | 短信签名ID |
| data[].signContent | string | 短信发送的签名内容 |
| data[].suffixSignId | int | 后缀签名ID |
| data[].suffixSign | string | 后缀信息 |
| data[].productId | int | 产品ID |
| data[].templateName | string | 模板名称 |
| data[].content | string | 模板内容 |
| data[].status | int | 审核状态：1=审核通过，2=待审核，3=审核驳回 |
| data[].ctime | string | 创建时间 |
| data[].userId | int | 用户ID |
| data[].userName | string | 用户名 |
| data[].refuseReason | string | 拒绝原因 |
| data[].postCount | int | 使用次数 |
| data[].sendCount | int | 发送量（条） |
| message | string | 返回信息 |
| timestamp | int | 时间戳 |
| status | string | 状态码 |

### 12.7 请求示例

```json
{
    "AppId": "100************77",
    "Version": "1.2.0",
    "MchId": "100**08",
    "Signature": "4B9CDDE1A12BCF822BA378A3C87DCDCE",
    "SignType": "MD5",
    "TimeStamp": "1652845494466"
}
```

### 12.8 返回示例

```json
{
    "data": [
        {
            "user": null,
            "upmsSign": null,
            "id": 70028082,
            "signId": 71597,
            "signContent": "【联麓信息测试】",
            "suffixSignId": -1,
            "suffixSign": "",
            "productId": 1001,
            "templateName": "test",
            "content": "嘿，{%name%}您好，您的测试账户是{%id%}",
            "status": 1,
            "ctime": "2023-07-13T08:11:35.000+00:00",
            "userId": 1027779,
            "userName": "QP",
            "refuseReason": "",
            "channelCmc": null,
            "channelCuc": null,
            "channelCtc": null,
            "access": null,
            "type": null,
            "remark": null,
            "limitDay": null,
            "postCount": 9,
            "sendCount": 9
        }
    ],
    "message": "success",
    "timestamp": 1652845495307,
    "status": "00"
}
```

### 12.9 错误码

参阅 API 错误代码。

---

## 13. 模板状态推送（回调）

### 13.1 概览

模板审核状态回调通知。对接方进入客户端对应的产品 > 系统管理 > 产品配置，配置推送地址。

### 13.2 请求URL

由用户在控制台配置回调地址。

### 13.3 请求方式

```
POST
```

### 13.4 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 13.5 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| productId | String | 产品类型：1001=通知短信，1002=营销短信，1003=国际短信，1004=语音服务，1007=视频彩信 |
| status | String | 审核状态：1=通过，3=驳回 |
| id | String | 模板ID |
| title | String | 固定值：`模板审核` |
| content | String | 推送信息，格式：`您的模板：{id}审核成功/审核失败` |
| type | String | 固定值：`template` |
| cTime | String | 推送时间 |

### 13.6 请求示例

```json
{
    "productId": "100*",
    "status": "1",
    "id": "70000001",
    "title": "模板审核",
    "content": "您的模板：70000001审核成功",
    "type": "template",
    "cTime": "1681868157273"
}
```

### 13.7 错误码

参阅 API 错误代码。

---

## 14. 产品余额查询

### 14.1 概览

通用产品余额查询接口，区分通知、营销短信。

### 14.2 请求URL

```
POST https://apis.shlianlu.com/sms/product/balance
```

### 14.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 14.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业id，每个账户企业id固定，登录查询 |
| AppId | String | 是 | 应用id，区分通知短信、营销短信，登录网站进入产品概览页查询 |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 14.5 Signature 签名机制

签名串拼接方式:

```
AppId=xxx&MchId=xxx&SignType=MD5&TimeStamp=1650784003&Version=1.2.0&key=xxx
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 14.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| balance | string | 当前账户余额 |
| message | string | 返回信息 |
| timestamp | string | 时间戳 |
| status | string | 状态码 |

### 14.7 请求示例

```json
{
    "MchId": "100***66",
    "AppId": "10*************00",
    "Version": "1.2.0",
    "TimeStamp": "1647485425382",
    "Signature": "0834F17B4C445571A3C6D2FE8F827EAD",
    "SignType": "MD5"
}
```

### 14.8 返回示例

```json
{
    "balance": 8937,
    "message": "success",
    "timestamp": 1651137039879,
    "status": "00"
}
```

### 14.9 错误码

参阅 API 错误代码。

---

## 15. 拉取报告

### 15.1 概览

根据 taskId 获取短信发送状态。

### 15.2 请求URL

```
POST https://apis.shlianlu.com/sms/trade/report
```

### 15.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 15.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| TaskId | String | 是 | 任务ID |
| AppId | String | 是 | 短信 SdkAppId，在短信控制台添加应用后生成 |
| MchId | String | 是 | 企业ID |
| Version | String | 是 | 公共参数，本接口取值：`1.2.0` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| pageNo | Number | 否 | 第几页，从1开始，默认1 |
| pageSize | Number | 否 | 每页显示多少条，默认10条 |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 15.5 Signature 签名机制

签名串拼接方式:

```
AppId=XXX&MchId=XXX&SignType=MD5&TaskId=XXX&TimeStamp=XXX&Version=1.2.0&pageNo=XXX&pageSize=XXX&key=XXX
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 15.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| data | object[] | 发送数据信息 |
| data[].sequenceId | string | 唯一值 |
| data[].phone | string | 手机号 |
| data[].content | string | 短信明细 |
| data[].status | string | 短信发送状态：0=未知，1=发送成功，2=发送失败 |
| data[].respTime | string | 短信接受时间，状态未知时为空 |
| data[].respCode | string | 短信状态码，`DELIVRD`=发送成功，其他为发送失败 |
| data[].codeDesc | string | 回执码中文描述 |
| message | string | 返回信息 |
| timestamp | string | 时间戳 |
| status | string | 状态码 |

### 15.7 请求示例

```json
{
    "TaskId": "202203150017868000003",
    "AppId": "10*************00",
    "Version": "1.2.0",
    "MchId": "100***66",
    "Signature": "0834F17B4C445571A3C6D2FE8F827EAD",
    "pageNo": 1,
    "TimeStamp": "1647485425382",
    "pageSize": 10,
    "SignType": "MD5"
}
```

### 15.8 返回示例

```json
{
    "data": [
        {
            "sequenceId": "100941727",
            "phone": "15601862749",
            "content": "【联麓信息】您正在验证，验证码123123，切勿将验证码泄露于他人，本条验证码有效期15分钟。",
            "status": "1",
            "respTime": "2022-04-10 17:10:20",
            "respCode": "DELIVRD",
            "codeDesc": "发送成功"
        }
    ],
    "message": "success",
    "timestamp": 1650680257674,
    "status": "00"
}
```

### 15.9 错误码

参阅 API 错误代码。

---

## 16. 拉取报告（消费模式）

### 16.1 概览

需要单独给账号做配置。获取已获取状态数据，最多返回 2000 条，已经拉取的数据不再展示。与标准拉取报告接口共用 URL，通过 Version 区分。

### 16.2 请求URL

```
POST https://apis.shlianlu.com/sms/trade/report
```

### 16.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 16.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| MchId | String | 是 | 企业ID |
| AppId | String | 是 | 短信 SdkAppId，在短信控制台添加应用后生成 |
| Version | String | 是 | 公共参数，本接口取值：`1.1.0` |
| SignType | String | 是 | 加密方式，固定值：`MD5` 或 `HMACSHA256` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| Signature | String | 是 | 数字签名，详见签名方法 |

### 16.5 Signature 签名机制

签名串拼接方式:

```
AppId=XXX&MchId=XXX&SignType=MD5&TimeStamp=XXX&Version=1.1.0&key=XXX
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 16.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| data | object[] | 发送数据信息 |
| data[].taskId | string | 任务批次ID |
| data[].phone | string | 号码 |
| data[].status | string | 短信发送状态：1=发送成功，2=发送失败 |
| data[].respTime | string | 短信接受时间 |
| data[].respCode | string | 短信状态码，`DELIVRD`=发送成功，其他为发送失败 |
| data[].tag | string | 自定义标签，发送时传入的 tag 参数 |
| message | string | 返回信息 |
| timestamp | string | 时间戳 |
| status | string | 状态码 |

### 16.7 请求示例

```json
{
    "MchId": "100***66",
    "AppId": "10*************00",
    "Version": "1.1.0",
    "SignType": "MD5",
    "TimeStamp": "1647485425382",
    "Signature": "F131BAFED46DF14F3FD1762DEAB2E696"
}
```

### 16.8 返回示例

```json
{
    "data": [
        {
            "taskId": "2023*************4277",
            "phone": "18*******50",
            "status": "1",
            "respTime": "1697875128230",
            "respCode": "DELIVRD",
            "tag": "个性短信测试V1.0.0"
        }
    ],
    "message": "success",
    "timestamp": 1697875870868,
    "status": "00"
}
```

### 16.9 错误码

参阅 API 错误代码。

### 16.10 与标准拉取报告的区别

| 维度 | 标准模式（15） | 消费模式（16） |
|------|:---:|:---:|
| Version | `1.2.0` | `1.1.0` |
| TaskId | 必填 | 不需要 |
| 分页 | 支持 pageNo/pageSize | 不支持，最多2000条 |
| 数据重复 | 可重复拉取 | 已拉取不再展示 |
| 返回字段 | 含 content/sequenceId/codeDesc | 含 taskId/tag |
| 配置 | 无需额外配置 | 需单独配置账号 |

---

## 17. 短信回执推送（回调）

### 17.1 概览

短信发送状态回调推送。配置路径：对应产品页面 > 通用管理 > 产品配置 > 推送配置 > 发送状态推送地址。

### 17.2 请求URL

由用户在控制台配置回调地址。

### 17.3 请求方式

```
POST
```

### 17.4 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 17.5 推送参数

| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | string | 唯一请求 ID，每次请求都会返回，定位问题时需要提供 |
| sequenceId | string | 序列ID |
| phone | string | 用户接受号码 |
| resptime | string | 用户接受时间 |
| respCode | string | 短信回执状态码，`DELIVRD`=接受成功，其他为接受失败 |
| codeDesc | string | respCode 状态码中文描述 |
| status | string | 推送状态，`00` |
| message | string | 返回信息，`success` |
| tag | string | 自定义标签，发送时传入的 tag 参数 |

### 17.6 推送示例

**推送成功回执：**

```json
{
    "taskId": "202203150017868000003",
    "sequenceId": "107067357",
    "phone": "13000200000",
    "resptime": "1679740568845",
    "respCode": "DELIVRD",
    "codeDesc": "发送成功",
    "message": "success",
    "tag": "",
    "status": "00"
}
```

**推送失败回执：**

```json
{
    "taskId": "202203150017868000003",
    "sequenceId": "107067357",
    "phone": "13000200000",
    "resptime": "1679740568845",
    "respCode": "LLY:003",
    "codeDesc": "频繁发送失败",
    "message": "success",
    "tag": "",
    "status": "00"
}
```

### 17.7 推送成功判定与重试机制

- HTTP 200 认为推送成功
- 接收方可在响应 JSON 中附带参数 `llcode="0"` 
- HTTP 响应非 200 或 llcode 不等于 0 的会重推
- 重推间隔：30 分钟，循环 5 次

### 17.8 错误码

参阅 API 错误代码。

---

## 18. 短信回复推送（回调）

### 18.1 概览

短信回复推送。配置路径：对应产品页面 > 系统管理 > 产品配置 > 推送设置 > 短信回复推送地址。

### 18.2 请求URL

由用户在控制台配置回调地址。

### 18.3 请求方式

```
POST
```

### 18.4 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 18.5 推送参数

| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | string | 唯一请求 ID，每次请求都会返回，定位问题时需要提供 |
| phone | string | 号码 |
| sequenceId | string | 序列ID |
| contentDown | string | 下行内容（短信明细） |
| contentUp | string | 上行内容（回复内容） |
| status | string | 推送状态 |
| timestamp | string | 时间戳 |
| tag | string | 自定义标签，发送时传入的 tag 参数 |

### 18.6 推送示例

```json
{
    "taskId": "10706xxxxxx",
    "phone": "130****0000",
    "sequenceId": "107067357",
    "contentDown": "xxxx",
    "contentUp": "你好，联麓信息",
    "timestamp": 1647485562153,
    "status": "00"
}
```

### 18.7 推送成功判定与重试机制

- HTTP 200 认为推送成功
- 接收方可在响应 JSON 中附带参数 `llcode="0"`
- HTTP 响应非 200 或 llcode 不等于 0 的会重推
- 重推间隔：30 分钟，循环 5 次

### 18.8 错误码

参阅 API 错误代码。

---

## 19. 拉取回复

### 19.1 概览

按天拉取回复。已拉取的回复，平台阅读状态变为"已读"，再次拉取不再展示。

### 19.2 请求URL

```
POST https://apis.shlianlu.com/sms/trade/reply
```

### 19.3 请求包头

```
Accept: application/json
Content-Type: application/json;charset=utf-8
```

### 19.4 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Version | string | 是 | 公共参数，本接口取值：`1.2.0` |
| AppId | String | 是 | 短信 SdkAppId，在短信控制台添加应用后生成 |
| MchId | String | 是 | 企业ID |
| Date | String | 是 | 回复日期，格式如 `20230828` |
| TimeStamp | String | 是 | UNIX 时间戳 |
| SignType | String | 是 | 固定值：`MD5` 或 `HMACSHA256` |
| Signature | String | 是 | 数字签名，详见签名方法 |
| pageNo | number | 否 | 第几页，从1开始 |
| pageSize | number | 否 | 每页显示多少条，最多100条 |

### 19.5 Signature 签名机制

签名串拼接方式:

```
AppId=XXX&Date=20230828&MchId=XXX&SignType=MD5&TimeStamp=XXX&Version=1.2.0&pageNo=1&pageSize=10&key=XXX
```

使用指定加密方式（MD5 或 HMACSHA256）加密后取**大写**字符串。

### 19.6 返回参数

| 参数 | 类型 | 说明 |
|------|------|------|
| total | int | 总条数 |
| data | object[] | 回复数据信息 |
| data[].taskId | string | 任务批次ID |
| data[].phone | string | 号码 |
| data[].respTime | string | 回复时间 |
| data[].respContent | string | 回复内容 |
| data[].tag | string | 自定义标签 |
| message | string | 返回信息 |
| timestamp | string | 时间戳 |
| status | string | 状态码 |

### 19.7 请求示例

```json
{
    "Date": "20230828",
    "AppId": "10011****16680",
    "Version": "1.2.0",
    "MchId": "10***79",
    "Signature": "F131BAFED46DF14F3FD1762DEAB2E696",
    "pageNo": 1,
    "TimeStamp": "1647485425382",
    "pageSize": 10,
    "SignType": "MD5"
}
```

### 19.8 返回示例

```json
{
    "total": 1,
    "data": [
        {
            "taskId": "202308281017110003555",
            "phone": "130****6601",
            "respTime": "2023-08-28 17:13:55",
            "respContent": "222222",
            "tag": ""
        }
    ],
    "message": "success",
    "timestamp": 1650680257674,
    "status": "00"
}
```

### 19.9 错误码

参阅 API 错误代码。

---

## 附录：签名机制详解

### 概述

为保证安全使用，在调用 API 时联麓会对每个请求通过签名 `Signature` 进行身份验证。无论使用 HTTP 还是 HTTPS 协议，都需要在请求中包含签名信息。

### 签名过程

1. 对请求中所有参数名首字母按照 26 个字母的顺序进行排序
2. 过滤以下参数名：`PhoneNumberSet`, `SessionContext`, `SessionContextSet`, `ContextParamSet`, `TemplateParamSet`, `Signature`, `PhoneList`, `phoneSet`
3. 过滤参数值为 null 或空字符串的参数
4. 将剩余参数按排序顺序拼接为 `key=value&key=value&...` 格式
5. 末尾拼接 `&key=AppKey`（AppKey 为平台密钥）
6. 将得到的字符串进行 MD5（或 HMACSHA256）加密，取大写

### 签名示例

以普通短信发送为例，请求体参数排序后：

```
AppId、MchId、PhoneNumberSet、SessionContext、SignName、SignType、Signature、Tag、TaskTime、TimeStamp、Type、Version
```

过滤掉 `PhoneNumberSet`、`SessionContext`、`Signature` 后，拼接：

```
AppId=1001xxx&MchId=1xxx9&SignName=【联麓】&SignType=MD5&Tag=123&TimeStamp=1694159054&Type=1&Version=1.2.0&key=6fxxxx50
```

MD5 加密后取大写，得到 `Signature` 值。

### Java 签名代码

```java
public String sign(Map<String, Object> params, String appKey) throws NoSuchAlgorithmException {
    // 请求中所有参数名排序，参数过滤后按顺序拼接
    String str = params.entrySet().stream()
            .filter(e -> (!e.getKey().equals("Signature")
                && !e.getKey().equals("SessionContext")
                && !e.getKey().equals("PhoneNumberSet")
                && !e.getKey().equals("SessionContextSet")
                && !e.getKey().equals("ContextParamSet")
                && !e.getKey().equals("TemplateParamSet")
                && !e.getKey().equals("PhoneList")
                && !e.getKey().equals("phoneSet")
                &&  e.getValue() != null
                && !e.getValue().toString().isEmpty()
             ))
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            // 结尾拼接上appkey
            .collect(Collectors.joining("&")) + "&key=" + appKey;
    // 拼接后字符串MD5加密
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(str.getBytes("UTF-8"));
    byte[] digest = md.digest();
    StringBuilder sb = new StringBuilder();
    for (byte b : digest) {
        sb.append(String.format("%02x", b & 0xff));
    }
    // 加密后大写
    return sb.toString().toUpperCase();
}
```

---

## 20. 企业微信 Secret

| 凭证 | 值 |
|------|----|
| 企业ID（CorpID） | `ww495d69a058868f65` |
| 企业微信 AgentID | `1000002` |
| 企业微信 Secret | `v_UZ3xPG0TiICP1ZSD_1IWHSPPdVAGLTmdFtaaZEJUE` |
```
