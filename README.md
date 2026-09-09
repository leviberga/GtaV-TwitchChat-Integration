# 🎮📞 GTA V Twitch Calls Integration

Ponte entre o chat da Twitch e o GTA V: mensagens do chat viram **ligações telefônicas dentro do jogo**, faladas por uma voz gerada por IA (TTS local, sem depender de API paga).

> Este repositório é a metade **Java** do projeto. A outra metade, do mod em C# que roda dentro do GTA V, vive em [`TwitchPhoneMod`](#) *(https://github.com/leviberga/GtaTwitchPhoneMod)*.

---

## ✨ Como funciona

```
Twitch Chat  ──"!ligar <mensagem>"──▶  TwitchChatListener
                                             │
                                             ▼
                                         CallQueue  ◀── LIGACAO_CONCLUIDA ──┐
                                             │                              │
                                             ▼                              │
                                      CallOrchestrator                      │
                                        │         │                        │
                                        ▼         ▼                        │
                                  TtsService   GtaWebSocketClient           │
                                  (Kokoro)     (TOCAR_LIGACAO|...)──▶ GTA V Mod
                                                                             │
                                        (mod avisa quando a ligação termina)┘
```

1. Um espectador manda `!ligar <mensagem>` no chat.
2. A mensagem entra na **fila** (`CallQueue`) — garante que várias ligações simultâneas toquem uma de cada vez, na ordem certa.
3. O texto é enviado pro **Kokoro-TTS** (rodando localmente), que gera um `.wav`.
4. O áudio é salvo direto na pasta de scripts do GTA V.
5. Um comando `TOCAR_LIGACAO|nome|texto` é enviado via WebSocket pro mod C#.
6. O jogo toca a ligação; ao terminar, o mod avisa de volta (`LIGACAO_CONCLUIDA`) e a fila libera a próxima.

---

## 📦 Requisitos

- **Java 17+**
- **Maven**
- **[Kokoro-TTS](https://github.com/remsky/Kokoro-FastAPI)** rodando localmente (via Docker), expondo `http://localhost:8880`
- **GTA V Enhanced** aberto, com o [mod C# companheiro](#) instalado e rodando
- Uma conta de bot na Twitch com um **OAuth token de chat** ([gerar aqui](https://twitchtokengenerator.com/))

---

## ⚙️ Configuração

Crie um arquivo `.env` na raiz do projeto:

```env
TWITCH_OAUTH_TOKEN=seu_token_aqui
TWITCH_CHANNEL=nome_do_canal
```

> ⚠️ O `.env` já está no `.gitignore`. Nunca commite seu token.

---

## ▶️ Rodando

```bash
mvn clean install
```

**Modo de teste local** (sem depender da Twitch, dispara uma ligação mockada):
```bash
mvn compile exec:java -Dexec.mainClass="com.github.leviberga.gtabridge.Main" -Dexec.args="--mock"
```

**Modo real** (conecta na Twitch e escuta o chat):
```bash
mvn compile exec:java -Dexec.mainClass="com.github.leviberga.gtabridge.Main"
```

Em ambos os casos, o GTA V já precisa estar aberto com o mod C# rodando — o Java se conecta nele via WebSocket em `ws://127.0.0.1:8080`.

---

## 💬 Comando no chat

```
!ligar <qualquer coisa que a pessoa quiser que o personagem ouça>
```

Por enquanto liberado pra qualquer um no chat usar.

---

## 🗂️ Estrutura do projeto

```
com.github.leviberga.gtabridge
├── Main.java                      # ponto de entrada — monta tudo e escolhe o modo
├── client/
│   └── GtaWebSocketClient.java    # conexão WebSocket única com o mod do GTA V
├── config/
│   └── AppConfig.java             # carrega .env / variáveis de ambiente
├── core/
│   └── CallOrchestrator.java      # TTS + envio pro jogo (fluxo compartilhado)
├── model/
│   └── CallRequest.java           # nome do chamador + texto
├── queue/
│   └── CallQueue.java             # fila de ligações simultâneas
├── tts/
│   └── TtsService.java            # integração com o Kokoro-TTS local
└── twitch/
    └── TwitchChatListener.java    # escuta o chat, parseia !ligar
```

---

## 🛣️ Roadmap

- [x] Geração de voz local via Kokoro-TTS
- [x] Comunicação WebSocket com o mod do GTA V
- [x] Fila para ligações simultâneas
- [x] Integração real com o chat da Twitch
- [ ] Regras de acesso ao comando (livre / subs / mods / Pontos do Canal / Bits)