/*
 * MotionQueue.h - Arduino library for queuing up motor events
 * Created by SDP Group 1 2014
 */
#ifndef _MOTIONQUEUE_H_
#define _MOTIONQUEUE_H_

#include "Arduino.h"

#define QUEUE_SIZE 16

typedef struct
{
    byte power[3];
    byte direction[3];
    unsigned short millis;
} queued_motion_t;

class MotionQueue
{
    public:
        MotionQueue();
        // 0 for success; -1 for queue full
        int enqueue(byte pow1, byte dir1, byte pow2, byte dir2, byte pow3, byte dir3, unsigned short ms);
        void pop();
        void clear();
        queued_motion_t* current();
        int capacity();
        int count();
        int update();
        bool full();
    private:
        queued_motion_t queue[QUEUE_SIZE + 1];
        int write_head;
        int read_head;
        void advance_read();
        void advance_write();
        unsigned long last_update;

};

#endif // _MOTIONQUEUE_H_