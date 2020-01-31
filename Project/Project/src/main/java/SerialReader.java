import purejavacomm.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class SerialReader {

    private File file;
    private PureJavaSerialPort serialPort;
    private Scanner scanner;

    public SerialReader(File file){
        this.file = file;
    }

    public void read(SerialListener listener) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException {

        serialPort = (PureJavaSerialPort) CommPortIdentifier
                .getPortIdentifier(file.getAbsolutePath())
                .open(getClass().getName(), 0);
        serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        scanner = new Scanner(serialPort.getInputStream());

        while (scanner.hasNext()) {
            listener.next(scanner.nextLine());
        }

        close();
        listener.onFinish();
    }

    public void close() {
        if (serialPort != null) {
            serialPort.close();
        }
        if (scanner != null) {
            scanner.close();
        }
    }
}
