package sdp.util;

import java.nio.ByteBuffer;

/**
 * Created by Matthew on 28/01/2015.
 */
public class CircularByteBuffer {
    /*
     * This class defines a circular buffer interface on top of a ByteBuffer.
     *
     */
    private int readHeadPosition;
    private int writeHeadPosition;
    private ByteBuffer backingBuffer;

    private CircularByteBuffer(ByteBuffer backing){
        this.readHeadPosition = 0;
        this.writeHeadPosition = 0;
        this.backingBuffer = backing;
    }

    public static CircularByteBuffer allocate(int size){
        return new CircularByteBuffer(ByteBuffer.allocate(size + 1));
    }

    public static CircularByteBuffer allocateDirect(int size){
        return new CircularByteBuffer(ByteBuffer.allocateDirect(size + 1));
    }

    public byte read() throws Exception {
        if(Math.abs(this.readHeadPosition - this.writeHeadPosition) > 0) {
            byte value = backingBuffer.get(this.readHeadPosition);
            this.advanceReadHead();
            return value;
        }

        // TODO: Specify this exception
        throw new Exception();
    }

    public byte peek() throws Exception {
        if(Math.abs(this.readHeadPosition - this.writeHeadPosition) > 0){
            return backingBuffer.get(this.readHeadPosition);
        }

        // TODO: Specify this exception
        throw new Exception();
    }

    public void discard() throws Exception {
        if(Math.abs(this.readHeadPosition - this.writeHeadPosition) == 0) {
            // TODO: Specify this exception
            throw new Exception();
        }

        this.advanceReadHead();
    }

    public CircularByteBuffer write(byte b){
        backingBuffer.put(this.writeHeadPosition, b);
        this.advanceWriteHead();
        if(this.writeHeadPosition == this.readHeadPosition) {
            this.advanceReadHead();
        }
        return this;
    }

    private void advanceWriteHead(int step){
        this.writeHeadPosition = (this.writeHeadPosition + step) % this.backingBuffer.capacity();
    }

    private void advanceWriteHead(){
        this.writeHeadPosition = (this.writeHeadPosition + 1) % this.backingBuffer.capacity();
    }

    private void advanceReadHead(int step){
        this.readHeadPosition = (this.readHeadPosition + step) % this.backingBuffer.capacity();
    }

    private void advanceReadHead(){
        this.readHeadPosition = (this.readHeadPosition + 1) % this.backingBuffer.capacity();
    }

    public boolean empty(){
        return this.readHeadPosition == this.writeHeadPosition;
    }

    public boolean full(){
        return (this.writeHeadPosition + 1) % this.backingBuffer.capacity() == this.readHeadPosition;
    }

    public CircularByteBuffer write(byte[] b, int count) throws Exception {
        if(count <= this.capacity()) {
            // TODO: Rewrite to use batch put
            for(int i = 0; i < count; i++){
                this.write(b[i]);
            }
            return this;
        }

        // TODO: Specify this exception
        throw new Exception();
    }

    public int elements() {
        if(this.readHeadPosition <= this.writeHeadPosition) {
            return this.writeHeadPosition - this.readHeadPosition;
        } else {
            return this.capacity() + (this.writeHeadPosition - this.readHeadPosition);
        }
    }

    public int capacity() {
        return this.backingBuffer.capacity() - 1;
    }

    public void read(byte[] buffer, int offset, int count) throws Exception {
        if(count < this.capacity()) {
            for (int i = offset; i < offset + count; i++) {
                buffer[i] = this.read();
            }
        }
    }
}