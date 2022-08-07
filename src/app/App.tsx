import { useEffect, useRef, useState } from "react";

interface Message {
  id: string;
  user: string;
  message: string;
}

function useMessages() {
  const [messages, setMessages] = useState<Message[]>([]);
  useEffect(() => {
    let ws = new WebSocket(
      `ws://${
        (import.meta as any).env.VITE_WS_PROXY_SERVER ||
        (window as any).location.host
      }/ws`
    );
    ws.addEventListener("message", (ev) => {
      let msg = JSON.parse(ev.data) as Message;
      if (msg) {
        setMessages((m) => {
          let arr = m.slice(-100);
          arr.push(msg);
          return arr;
        });
      }
    });

    return () => {
      switch (ws.readyState) {
        case ws.CONNECTING:
          ws.addEventListener("open", () => {
            ws.close();
          });
          break;
        case ws.OPEN:
          ws.close();
          break;
        default:
          break;
      }
    };
  }, []);

  return messages;
}

function App() {
  const messages = useMessages();
  const messageRef = useRef<HTMLInputElement>(null);

  return (
    <div className="App">
      <h1>Chat</h1>
      <form
        onSubmit={(e) => {
          e.preventDefault();

          let form = e.target as any;

          let user = form.user?.value;
          let message = form.message?.value;

          fetch("/api/messages", {
            method: "post",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ user, message }),
          });

          messageRef.current!.value = "";
        }}
      >
        <input name="user" placeholder="Username" />
        <input name="message" ref={messageRef} placeholder="Message" />
        <button>Submit</button>
      </form>
      <ul className="messages">
        {messages.map((m) => (
          <li className="message" key={m.id}>
            <span className="user">{m.user}:</span> {m.message}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
