# 🎮 hVincular - Sistema Avançado de Verificação de YouTubers para Minecraft

<div align="center">
  <img src="https://i.imgur.com/QAIQ0z4.png" alt="Logo hVincular" width="150px"/>
  <br>
  <b>Vincule canais do YouTube com seu servidor Minecraft de forma simples e segura</b>
  <br><br>
</div>

O **hVincular** é um sistema completo de verificação que permite aos jogadores vincularem seus canais do YouTube e receberem tags, permissões e recompensas exclusivas de forma automática. O plugin analisa vários critérios como número de inscritos, visualizações e confirma a propriedade do canal através de um código de verificação único na descrição dos vídeos.

## ✨ Diferenciais do hVincular

<table>
  <tr>
    <td width="33%">
      <h3 align="center">🛡️ Segurança</h3>
      <ul>
        <li><b>Códigos únicos de verificação</b> para confirmar a propriedade do canal</li>
        <li><b>Sistema anti-fraude</b> com detecção de vinculações suspeitas</li>
        <li><b>Filtros configuráveis</b> para monitoramento de tags específicas</li>
        <li><b>Logs detalhados</b> para auditoria completa</li>
      </ul>
    </td>
    <td width="33%">
      <h3 align="center">⚙️ Flexibilidade</h3>
      <ul>
        <li><b>Compatibilidade total</b> do Minecraft 1.7 até 1.21</li>
        <li><b>Suporte a múltiplos bancos</b> (MySQL/SQLite com fallback automático)</li>
        <li><b>Múltiplas tags</b> com requisitos personalizáveis</li>
        <li><b>Mensagens e menus</b> totalmente customizáveis</li>
      </ul>
    </td>
    <td width="33%">
      <h3 align="center">🚀 Facilidade</h3>
      <ul>
        <li><b>Interface intuitiva</b> para seleção de tags</li>
        <li><b>Integração Discord</b> para monitoramento em tempo real</li>
        <li><b>Comandos simplificados</b> para administração</li>
        <li><b>Sistema de recompensas</b> totalmente configurável</li>
      </ul>
    </td>
  </tr>
</table>

## 📋 Características Principais

<details>
<summary><b>Interface Amigável e Verificação Robusta</b></summary>
<ul>
  <li>Menu intuitivo para seleção de tags com descrições claras dos requisitos</li>
  <li>Verificação automática e em tempo real de canais e vídeos do YouTube</li>
  <li>Suporte a cooldowns configuráveis para evitar abusos</li>
</ul>
</details>

<details>
<summary><b>Suporte Multi-plataforma e Adaptação Automática</b></summary>
<ul>
  <li>Compatibilidade com todas as versões do Minecraft Java (1.7 até 1.21)</li>
  <li>Funciona em servidores Bukkit, Spigot, Paper, Purpur e derivados</li>
  <li>Detecção e adaptação automática à versão do servidor</li>
</ul>
</details>

<details>
<summary><b>Gestão de Dados Flexível e Segura</b></summary>
<ul>
  <li>Suporte nativo a MySQL para ambientes em rede ou múltiplos servidores</li>
  <li>SQLite integrado como opção padrão para instalações simples</li>
  <li>Fallback automático entre sistemas de banco de dados para máxima confiabilidade</li>
  <li>Criptografia de dados sensíveis para máxima segurança</li>
</ul>
</details>

<details>
<summary><b>Integração Avançada com Discord</b></summary>
<ul>
  <li>Notificações em tempo real via webhooks para cada nova vinculação</li>
  <li>Sistema de marcação de vinculações suspeitas para revisão pela equipe</li>
  <li>Filtros de tags para controlar quais verificações geram notificações</li>
  <li>Personalização completa de mensagens, embeds, cores e menções</li>
  <li>Suporte nativo a emojis Unicode em todas as mensagens</li>
</ul>
</details>

<details>
<summary><b>Sistema de Detecção de Fraudes</b></summary>
<ul>
  <li>Identificação automática de vinculações potencialmente suspeitas</li>
  <li>Critérios configuráveis como número mínimo de inscritos ou número máximo de verificações por jogador</li>
  <li>Alertas diferenciados no Discord para facilitar a moderação</li>
  <li>Sistema anti-abusos para proteger seu servidor contra falsificações</li>
</ul>
</details>

<details>
<summary><b>Internacionalização e Personalização</b></summary>
<ul>
  <li>Suporte completo a caracteres UTF-8 e emojis em todas as mensagens</li>
  <li>Sistema de mensagens totalmente configurável através do arquivo messages.yml</li>
  <li>Personalização visual dos menus e notificações</li>
</ul>
</details>

## 🔧 Requisitos

- Servidor Bukkit/Spigot/Paper (versão 1.7+)
- Plugin de permissões compatível com [Vault](https://www.spigotmc.org/resources/vault.34315/) (recomendamos LuckPerms)
- Chave de API do YouTube (instruções abaixo)
- Java 8 ou superior (compatibilidade total com Java 8 para servidores antigos)

## 💿 Instalação

1. Baixe o arquivo JAR da seção releases ou compile o projeto
2. Coloque o arquivo na pasta `plugins` do seu servidor
3. Inicie o servidor para gerar os arquivos de configuração
4. Configure sua chave de API do YouTube em `config.yml`
5. Personalize suas tags em `menus.yml`
6. Reinicie o servidor e comece a usar!

### Compilação do Projeto

<details>
<summary>Usando Maven</summary>

```bash
mvn clean package
```
O arquivo compilado estará em: `target/hVincular-1.0.0.jar`
</details>

<details>
<summary>Usando Gradle</summary>

```bash
./gradlew clean shadowJar
```
O arquivo compilado estará em: `build/libs/hvincular-1.0.0.jar`
</details>

## 🛠️ Configuração

O plugin possui vários arquivos de configuração para personalização completa:

### 🔑 Chave da API do YouTube

Para o funcionamento correto do plugin, você precisa de uma chave de API do YouTube:

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto
3. Ative a API do YouTube Data v3
4. Crie credenciais para obter uma chave de API
5. Adicione a chave em `config.yml`

### 💾 Bancos de Dados

O plugin suporta dois sistemas de banco de dados:

<details>
<summary><b>SQLite (padrão)</b></summary>
Habilitado automaticamente, não necessita configuração adicional. Ideal para servidores únicos.
</details>

<details>
<summary><b>MySQL (opcional)</b></summary>

```yaml
database:
  enabled: true              # Ative para usar MySQL
  type: "mysql"
  host: "localhost"
  port: 3306
  database: "hvincular"
  username: "seu_usuario"
  password: "sua_senha"
  table-prefix: "hv_"
```
Ideal para redes de servidores, mantendo vinculações sincronizadas entre múltiplos servidores.
</details>

### 🤖 Integração com Discord

<details>
<summary><b>Configuração básica do webhook</b></summary>

1. Crie um webhook no seu servidor Discord (Configurações > Integrações > Webhooks)
2. Copie a URL do webhook
3. Configure em `webhook.yml`:
```yaml
enabled: true
webhook-url: "sua_url_do_webhook"
webhook-username: "Sistema de Verificação YouTuber"
webhook-avatar-url: "url_opcional_para_avatar"
```
</details>

<details>
<summary><b>Recursos avançados do Discord</b></summary>

#### 🔍 Detecção de Verificações Suspeitas

O plugin pode identificar automaticamente vinculações potencialmente fraudulentas:

```yaml
suspicious-detection:
  enabled: true                     # Ativa o sistema de detecção
  min-subscribers: 100              # Número mínimo de inscritos para não ser suspeito
  max-verifications-per-player: 3   # Número máximo de verificações por jogador
  verification-period-days: 7       # Período de contagem de verificações
  highlight-with-color: true        # Usar cor diferente para casos suspeitos
```

#### 🏷️ Filtro de Tags

Controle quais tags enviam notificações para o Discord:

```yaml
tag-filters:
  youtuber: true     # Envia notificações para a tag "youtuber"
  miniyt: true       # Envia notificações para a tag "miniyt"
  streamer: false    # NÃO envia notificações para a tag "streamer"
```

#### 📢 Menções a Cargos

Configure menções automáticas para alertar sua equipe:

```yaml
mentions:
  enabled: true
  role-id: "123456789012345678"   # ID do cargo a ser mencionado
```

Para mencionar @everyone ou @here, use `"everyone"` ou `"here"` como ID do cargo.
</details>

### 🎯 Sistema de Tags e Recompensas

Configure tags personalizadas com requisitos próprios em `menus.yml`:

```yaml
youtuber:
  name: "YouTuber"
  permission: "tag.youtuber"
  min-subscribers: 1000           # Mínimo de inscritos para esta tag
  min-views: 5000                 # Mínimo de visualizações no canal
  cooldown-seconds: 604800        # Cooldown de 7 dias entre verificações
  menu-slot: 11                   # Posição no menu de seleção

  # Aparência no menu
  item:
    material: "DIAMOND"
    display-name: "&b&lTag YouTuber"
    lore:
      - "&7Para criadores com pelo menos &e1.000 &7inscritos"
      - "&7e &e5.000 &7visualizações totais no canal."
      - ""
      - "&eClique para vincular seu canal!"

  # Recompensas automáticas
  rewards:
    enabled: true
    commands:
      - "eco give {player} 10000"
      - "lp user {player} parent add youtuber"
      - "broadcast &b{player} &fé agora um &bYouTuber &foficial!"
```

## 📝 Comandos

### Para Jogadores:
- `/vincular` - Abre o menu de vinculação
- `/vincular <url>` - Inicia a vinculação com uma URL de vídeo específica
- `/desvincular` - Remove sua própria vinculação atual (se permitido na configuração)

### Para Administradores:
- `/hv help` - Mostra a lista de comandos
- `/hv reload` - Recarrega a configuração do plugin
- `/hv version` - Mostra a versão atual do plugin
- `/hv status` - Verifica o status do banco de dados e API do YouTube
- `/hdesvincular <jogador> [tag]` - Remove vinculação de um jogador

## 🧩 Placeholder e Variáveis

O plugin oferece diversas variáveis para uso nas mensagens, comandos e recompensas:

- `{player}` - Nome do jogador
- `{tag}` - Nome da tag vinculada
- `{permission}` - Permissão associada à tag
- `{channel}` - ID do canal do YouTube
- `{channel_url}` - URL completa do canal
- `{video_url}` - URL do vídeo usado na verificação
- `{subscribers}` - Número de inscritos do canal
- `{views}` - Total de visualizações do canal
- `{datetime}` - Data e hora da vinculação
- `{role_mention}` - Menção ao cargo no Discord (se configurado)

## 🔒 Segurança e Privacidade

O hVincular foi desenvolvido com foco em segurança:

- Códigos de verificação únicos para cada jogador
- Sistema anti-fraude com detecção de verificações suspeitas
- Validação rigorosa de dados de entrada
- Sanitização de todas as consultas ao banco de dados
- Proteção contra injeção de comandos

## ❓ Suporte e Contato

Se você tiver dúvidas, sugestões ou precisar de ajuda:

- **GitHub:** [github.com/Hokase](https://github.com/)
- **Discord:** hokase
- **YouTube:** [youtube.com/@SoOHokase](https://youtube.com/@SoOHokase)

---

<div align="center">
  <p>Desenvolvido com ❤️ por Hokase</p>
  <p>hVincular © 2024 | Versão 1.0.0</p>
</div>
#   h V i n c u l a r  
 