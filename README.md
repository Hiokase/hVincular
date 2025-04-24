
# 🎮 hVincular - Sistema Avançado de Verificação de YouTubers para Minecraft

**Vincule canais do YouTube com seu servidor Minecraft de forma simples e segura**

![Logo hVincular](https://i.imgur.com/QAIQ0z4.png)

---

O **hVincular** é um plugin completo para servidores Minecraft que permite aos jogadores vincularem seus canais do YouTube e receberem tags, permissões e recompensas exclusivas automaticamente.

## ✨ Destaques

### 🛡️ Segurança
- Códigos únicos de verificação
- Sistema anti-fraude e monitoramento de vinculações suspeitas
- Filtros configuráveis por tag
- Logs detalhados

### ⚙️ Flexibilidade
- Compatível com Minecraft 1.7 até 1.21
- Suporte a MySQL e SQLite
- Múltiplas tags personalizáveis
- Mensagens e menus customizáveis

### 🚀 Facilidade
- Menu intuitivo de vinculação
- Integração com Discord (webhooks)
- Comandos simplificados
- Sistema de recompensas customizado

---

## 📋 Funcionalidades

- Interface amigável com menus interativos
- Verificação automática de canais e vídeos do YouTube
- Suporte a cooldowns configuráveis
- Compatibilidade com Bukkit, Spigot, Paper, Purpur
- Adaptação automática à versão do servidor
- Integração com Discord: notificações, menções, filtros de tag
- Detecção de fraudes com alertas automáticos
- Sistema de mensagens e menus 100% configurável
- Suporte completo a UTF-8 e emojis

---

## 🔧 Requisitos

- Servidor Bukkit/Spigot/Paper 1.7+
- Java 8 ou superior
- Chave da API do YouTube (YouTube Data API v3)

---

## 💿 Instalação

1. Baixe o plugin na aba Releases ou compile manualmente
2. Coloque o JAR na pasta `plugins`
3. Inicie o servidor para gerar os arquivos
4. Configure a chave da API no `config.yml`
5. Personalize as tags no `menus.yml`
6. Reinicie o servidor

---

## 🔨 Compilação

### Maven
```bash
mvn clean package
```
Saída: `target/hVincular-1.0.0.jar`

### Gradle
```bash
./gradlew clean shadowJar
```
Saída: `build/libs/hVincular-1.0.0.jar`

---

## ⚙️ Configurações Importantes

### Chave da API do YouTube
1. Crie um projeto no [Google Cloud Console](https://console.cloud.google.com/)
2. Ative a API do YouTube Data v3
3. Gere uma chave de API e coloque em `config.yml`

### Banco de Dados

**SQLite (padrão)** – sem configuração necessária.  
**MySQL (recomendado para redes):**
```yaml
database:
  enabled: true
  type: "mysql"
  host: "localhost"
  port: 3306
  database: "hvincular"
  username: "usuario"
  password: "senha"
```

### Integração com Discord
```yaml
enabled: true
webhook-url: "URL_DO_WEBHOOK"
webhook-username: "Sistema de Verificação"
webhook-avatar-url: "URL_DO_AVATAR"
```

### Sistema de Detecção de Fraudes
```yaml
suspicious-detection:
  enabled: true
  min-subscribers: 100
  max-verifications-per-player: 3
```

---

## 🏷️ Exemplo de Tag

```yaml
youtuber:
  name: "YouTuber"
  permission: "tag.youtuber"
  min-subscribers: 1000
  min-views: 5000
  cooldown-seconds: 604800
  rewards:
    enabled: true
    commands:
      - "eco give {player} 10000"
      - "lp user {player} parent add youtuber"
```

---

## 💬 Comandos

### Jogadores
- `/vincular` – Abre o menu
- `/vincular <url>` – Vincula com vídeo
- `/desvincular` – Remove vinculação

### Admins
- `/hv reload` – Recarrega configs
- `/hv status` – Checa status de API e DB
- `/hdesvincular <jogador>` – Remove vinculação

---

## 🧩 Placeholders

- `{player}` – Nome do jogador
- `{tag}` – Nome da tag
- `{subscribers}` – Inscritos do canal
- `{video_url}` – URL usada na verificação

---

## 🔒 Segurança

- Sanitização de entradas
- Proteção contra fraudes e injeções
- Verificações únicas por jogador

---

## 📞 Suporte

- GitHub: [github.com/Hokase](https://github.com/Hokase)
- Discord: `hokase`
- YouTube: [@SoOHokase](https://youtube.com/@SoOHokase)

---

**Desenvolvido com ❤️ por Hokase**  
`hVincular © 2024 | Versão 1.0.0`
