package sdp.comms;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import sdp.comms.packets.*;
import sdp.util.CircularByteBuffer;

import java.nio.BufferUnderflowException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Queue;

/**
 * Created by Matthew on 16/01/2015.
 */
public class RadioController implements SerialPortEventListener {
    private SerialPort parent;
    private CircularByteBuffer buffer;
    private List<PacketListener> listeners;

    public RadioController(SerialPort parent, List<PacketListener> listeners) {
        this.parent = parent;
        this.buffer = CircularByteBuffer.allocate(10*1024);
        this.listeners = listeners;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
    	
        // Message received
        if(event.isRXCHAR()) {
            try {
                byte[] bytes = parent.readBytes(event.getEventValue());
                buffer.write(bytes, event.getEventValue());
            } catch (SerialPortException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            while(buffer.elements() > 0) {

                try {
                    byte peeked_id = buffer.peek();

                    Packet read = null;

                    // TODO: Refactor this unholy mess
                    if (peeked_id == ActivatePacket.ID) {
                        if (buffer.elements() < ActivatePacket.Length) {
                            break;
                        }

                        read = new ActivatePacket();
                    } else if (peeked_id == AcknowledgePacket.ID) {
                        if (buffer.elements() < AcknowledgePacket.Length) {
                            break;
                        }

                        read = new AcknowledgePacket();
                    } else if (peeked_id == DeactivatePacket.ID) {
                        if (buffer.elements() < DeactivatePacket.Length) {
                            break;
                        }

                        read = new DeactivatePacket();
                    } else if (peeked_id == DrivePacket.ID) {
                        if (buffer.elements() < DrivePacket.Length) {
                            break;
                        }

                        read = new DrivePacket();
                    } else if (peeked_id == ErrorPacket.ID) {
                        if (buffer.elements() < ErrorPacket.Length) {
                            break;
                        }

                        read = new ErrorPacket();
                    } else if (peeked_id == KickPacket.ID) {
                        if (buffer.elements() < KickPacket.Length) {
                            break;
                        }

                        read = new KickPacket();
                    } else if (peeked_id == SensorDataPacket.ID) {
                        if (buffer.elements() < SensorDataPacket.Length) {
                            break;
                        }

                        read = new SensorDataPacket();
                    } else if (peeked_id == MotionCompletePacket.ID) {
                        if (buffer.elements() < MotionCompletePacket.Length) {
                            break;
                        }

                        read = new MotionCompletePacket();
                    } else if (peeked_id == DisengageCatcherPacket.ID) {
                        if(buffer.elements() < DisengageCatcherPacket.Length) {
                            break;
                        }

                        read = new DisengageCatcherPacket();
                    } else if (peeked_id == EngageCatcherPacket.ID) {
                        if (buffer.elements() < EngageCatcherPacket.Length) {
                            break;
                        }

                        read = new EngageCatcherPacket();
                    } else if (peeked_id == CatcherStateToggledPacket.ID) {
                        if (buffer.elements() < CatcherStateToggledPacket.Length) {
                            break;
                        }

                        read = new CatcherStateToggledPacket();
                    } else if (peeked_id == BallStatePacket.ID) {
                        if (buffer.elements() < BallStatePacket.Length) {
                            break;
                        }

                        read = new BallStatePacket();
                    } else if (peeked_id == CalibrateCatcherPacket.ID){
                        if (buffer.elements() < CalibrateCatcherPacket.Length) {
                            break;
                        }

                        read = new CalibrateCatcherPacket();
                    } else if (peeked_id == KickerDecreasePacket.ID) {
                        if (buffer.elements() < KickerDecreasePacket.Length) {
                            break;
                        }

                        read = new KickerDecreasePacket();
                    } else if (peeked_id == KickerIncreasePacket.ID) {
                        if (buffer.elements() < KickerIncreasePacket.Length) {
                            break;
                        }

                        read = new KickerIncreasePacket();
                    } else if (peeked_id == BallCaughtPacket.ID) {
                        if (buffer.elements() < BallCaughtPacket.Length) {
                            break;
                        }

                        read = new BallCaughtPacket();
                    } else {
                        // Throw away garbage bytes
                        buffer.discard();
                        continue;
                    }
                    // Throw away ID byte before parsing rest of packet
                    buffer.discard();
                    read.readPacket(buffer);
                    this.dispatch(read);
                } catch (BufferUnderflowException e){
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void dispatch(Packet read) {
        for(PacketListener listener: listeners) {
            listener.packetArrived(read);
        }
    }
}
