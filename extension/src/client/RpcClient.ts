import * as rpc from "vscode-jsonrpc/node";
import * as net from "net";

export class RpcClient {
    private connection: rpc.MessageConnection | null = null;

    constructor(private readonly host: string, private readonly port: number) {
        this.connect();
    }

    private connect(): void {
        const socket = net.connect(this.port, this.host, () => {
            this.connection = rpc.createMessageConnection(
                new rpc.StreamMessageReader(socket),
                new rpc.StreamMessageWriter(socket)
            );
            this.connection!.listen();
        });

        socket.on("error", (err) => {
            console.error("Socket error:", err);
        });

        socket.on("close", () => {
            console.log("Socket closed");
            this.connection = null;
        });
    }

    public async sendRequest<T>(requestType: rpc.RequestType<any, T, any>, params?: any): Promise<T> {
        if (!this.connection) {
            throw new Error("Connection is not established");
        }
        return this.connection.sendRequest(requestType, params);
    }

    public disconnect(): void {
        if (this.connection) {
            this.connection.dispose();
            this.connection = null;
        }
    }
}
