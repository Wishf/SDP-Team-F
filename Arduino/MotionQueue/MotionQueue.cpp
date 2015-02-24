#include "Arduino.h"
#include "MotionQueue.h"

MotionQueue::MotionQueue()
{
    write_head = 0;
    read_head = 0;
    last_update = millis();
}

int MotionQueue::enqueue(byte pow1, byte dir1, byte pow2, byte dir2, byte pow3, byte dir3, unsigned short ms)
{
    if(full())
    {
        return -1;
    }

    queue[write_head].power[0] = pow1;
    queue[write_head].direction[0] = dir1;
    queue[write_head].power[1] = pow2;
    queue[write_head].direction[1] = dir2;
    queue[write_head].power[2] = pow3;
    queue[write_head].direction[2] = dir3;
    queue[write_head].millis = ms;
    advance_write();

    return 0;
}

void MotionQueue::pop()
{
    if(count() > 0){
        advance_read();
    }
}

void MotionQueue::clear()
{
    read_head = 0;
    write_head = 0;
}

queued_motion_t* MotionQueue::current()
{
    return &queue[read_head];
}

int MotionQueue::capacity()
{
    return QUEUE_SIZE;
}

int MotionQueue::count()
{
    int offset = write_head - read_head;

    if(read_head > write_head) {
        return QUEUE_SIZE + offset;
    }

    return offset;
}

int MotionQueue::update()
{
    unsigned long ms = millis();
    unsigned long delta = ms - last_update;

    // we can end the current action if we've reached its termination time
    if(count() > 0){
        queue[read_head].millis -= delta;
        if(queue[read_head].millis <= 0)
        {
            pop();
            return 1;
        }
    }

    last_update = ms;

    return 0;
}

void MotionQueue::advance_read()
{
    read_head = (read_head + 1) % (QUEUE_SIZE + 1);
}

void MotionQueue::advance_write()
{
    write_head = (write_head + 1) % (QUEUE_SIZE + 1);
}

bool MotionQueue::full()
{
    // If there's only one unused slot, we're full
    return (write_head + 1) % (QUEUE_SIZE + 1) == read_head;
}