package server;

import commands.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private String nickname;
    private String login;
    private String password;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // установка сокет тайм аут
                    socket.setSoTimeout(120000);

                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        //если команда отключиться
                        if (str.equals(Command.END)) {
                            out.writeUTF(Command.END);
                            throw new RuntimeException("Клиент захотел отключиться");
                        }

                        //если команда аутентификация
                        if (str.startsWith(Command.AUTH)) {
                            String[] token = str.split("\\s", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (newNick != null) {
                                if (!server.isLoginAuthenticated(login)) {
                                    nickname = newNick;
                                    sendMsg(Command.AUTH_OK + " " + nickname);
                                    server.subscribe(this);
                                    System.out.println("client: " + socket.getRemoteSocketAddress() +
                                            " connected with nick: " + nickname);
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("Данная учетная запись уже используется");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }

                        //если команда регистрация
                        if (str.startsWith(Command.REG)) {
                            String[] token = str.split("\\s", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            boolean regSuccess = server.getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (regSuccess) {
                                sendMsg(Command.REG_OK);
                            } else {
                                sendMsg(Command.REG_NO);
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                out.writeUTF(Command.END);
                                break;
                            }

                            if (str.startsWith(Command.PRIVATE_MSG)) {
                                String[] token = str.split("\\s", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                            // изменение ника
                            if (str.startsWith(Command.CHANGE_NICK)) {
                                String[] token = str.split("\\s", 2);
                                if (token.length < 2) {
                                    continue;
                                }
                                server.getAuthService().chengeNick(this.nickname,token[1]);
                                server.changeNik(this.nickname,token[1]);
                                System.out.println(this.nickname+" "+token[1]);

                            }


                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    sendMsg(Command.END);
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected: " + nickname);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }


    public String getLogin() {
        return login;
    }
}
