# Sofia Supermarkets API Документация MCP server

Sofia Supermarkets API Документация is available as a hosted [Model Context Protocol](https://modelcontextprotocol.io) (MCP)
server, so AI agents such as Claude, ChatGPT, Cursor and VS Code can call it as tools.

| | |
| --- | --- |
| Server URL | `https://app.getelva.ai/mcp/elvademo/sofia-supermarkets-api-` |
| Install page | https://app.getelva.ai/mcp/elvademo/sofia-supermarkets-api-/install |
| Tools | 3, one per operation in [`api-docs`](https://api.naoferta.net/v3/api-docs) |
| Calls | `https://sofia-supermarkets-api-proxy.stefan-bratanov.workers.dev` |
| Authentication | None, the API is public. |

## Install

**[Open the install page](https://app.getelva.ai/mcp/elvademo/sofia-supermarkets-api-/install)** for step-by-step setup in Claude, ChatGPT,
Cursor, VS Code and other clients, or use one of the options below.

[![Install in Cursor](https://cursor.com/deeplink/mcp-install-dark.svg)](https://cursor.com/en/install-mcp?name=Sofia%20Supermarkets%20API%20%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F&config=eyJ1cmwiOiJodHRwczovL2FwcC5nZXRlbHZhLmFpL21jcC9lbHZhZGVtby9zb2ZpYS1zdXBlcm1hcmtldHMtYXBpLSJ9)
[![Install in VS Code](https://img.shields.io/badge/VS_Code-Install_MCP-0098FF?logo=visualstudiocode&logoColor=white)](https://insiders.vscode.dev/redirect/mcp/install?name=sofia-supermarkets-api&config=%7B%22type%22%3A%22http%22%2C%22url%22%3A%22https%3A%2F%2Fapp.getelva.ai%2Fmcp%2Felvademo%2Fsofia-supermarkets-api-%22%7D)

### Claude Code

```bash
claude mcp add --transport http sofia-supermarkets-api https://app.getelva.ai/mcp/elvademo/sofia-supermarkets-api-
```

### Claude (claude.ai and Claude Desktop)

Settings → Connectors → **Add custom connector**, then paste the server URL.

### ChatGPT

Settings → Apps & Connectors → Advanced settings → turn on **Developer mode**, then
**Create** a connector and paste the server URL.

### Any other MCP client

```json
{
  "mcpServers": {
    "sofia-supermarkets-api": {
      "url": "https://app.getelva.ai/mcp/elvademo/sofia-supermarkets-api-"
    }
  }
}
```

---

Generated from the API's published OpenAPI spec and hosted by [Elva](https://getelva.ai?utm_source=github&utm_medium=StefanBratanov/sofia-supermarkets-api&utm_campaign=githubprs).
