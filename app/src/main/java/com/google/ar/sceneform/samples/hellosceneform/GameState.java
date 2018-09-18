package com.google.ar.sceneform.samples.hellosceneform;

import android.graphics.Point;
import android.view.MotionEvent;

import com.google.ar.core.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

public class GameState {
    private MotionEvent lastMotionEvent = null;
    private Point pointer = null;
    private boolean pointerDown = false;
    public Node Pokemon;
    public Node Ball;
    public Vector3 BallVelocity;
    public Vector3 BallAcceleration;
    public int HoldingBall;
    public Camera camera;
    public Point displaySize;

    // handles new MotionEvents
    public void newMotionEvent(MotionEvent e) {
        lastMotionEvent = e;
        pointer = new Point((int) e.getX(), (int) e.getY());
        switch (e.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                pointerDown = true;
                break;
            case (MotionEvent.ACTION_UP):
                pointerDown = false;
                break;
        }
    }

    public Point getPointer() {
        if (pointerDown) {
            return pointer;
        }
        return null;
    }

}
