import * as net from 'net';
import { Socket } from 'net';
import { Logger } from "../logger/index";

export class BuildServerClient {
    private server: net.Server;
    private readonly PIPE_PATH: string;
    private readonly logger: Logger

    constructor(Logger: Logger) {
        this.PIPE_PATH = "/tmp/example.sock";
        this.server = new net.Server();
        this.logger = Logger
        this.setupServer();
    }

    private setupServer(): void {
        this.server.on('connection', (socket: Socket) => {
            this.logger.info('Client connected.');

            socket.on('data', (data) => {
                this.logger.info('Received:', data.toString());
            });

            socket.on('end', () => {
                this.logger.info('Client disconnected.');
            });

        });

        this.server.on('error', (err) => {
            this.logger.error('Server error:', err.message);
        });

        this.server.listen(this.PIPE_PATH, () => {
            this.logger.info(`Server listening on ${this.PIPE_PATH}`);
        });
    }

    public dispose(): void {
        this.server.close(() => {
            console.log('Server closed');
        });
    }

}


