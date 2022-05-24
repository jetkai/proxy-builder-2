###### Proxy Builder 2.0

## Minimum Requirements: 

- ✅ IntelliJ 2020
- ✅ JDK 8
- ✅ Kotlin 1.5.0
- ✅ Internet
- ✅ MariaDB (or another SQL alternative)

## New features currently include:
- Location data
  - Country
  - Continent
  - (Etc...) 
- Provider data
  - ISP/Hosting provider
- Connection endpoint information
  - (How many times it had connceted to XYZ endpoint)
  - Ping
  - Uptime
- Merged proxies
  - Combining SOCKS4, SOCKS5, HTTP & HTTPS together along with the ports that match the protocol

View Types Available:
- Classic
  - Current view (http, https, socks4, socks5) as ip:port in an array
- Basic
  - Mapped data (ip -> "0.0.0.0", port -> 12345, ping -> 120) etc...
- Advanced
  - Mapped data, same format as basic but includes Location data + Provider Data etc...

```json
[{
  "ip" : "1.10.141.220",
  "port" : 54620,
  "ping" : 739,
  "protocols" : [ {
    "type" : "https",
    "port" : 54620
  }, {
    "type" : "http",
    "port" : 54620
  }, {
    "type" : "socks4",
    "port" : 37718
  } ]
}]
```
