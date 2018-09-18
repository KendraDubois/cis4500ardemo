package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
//import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class GameLoop extends AsyncTask<Void, Void, Void> {

    protected GameState state;
    private int frameNum;

    private Vector3[] trail;
    private int trailLen = 20;

    private LocalBroadcastManager bm;

    public GameLoop(GameState state, HelloSceneformActivity main) {
        this.state = state;

        // set up the brodcast manager for clearing the model
        bm = LocalBroadcastManager.getInstance(main);

        // set the gravity acting on the ball
        state.BallAcceleration = new Vector3(0f, -0.0001f, 0f);

        // set the initial state of the game
        state.HoldingBall = 0;

        // init the ball trail
        trail = new Vector3[trailLen];
    }

    @Override
    protected Void doInBackground(Void... args) {

        while (true) {
            tick();
            frameNum++;
            try {
                Thread.sleep(17);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {

    }

    protected void tick() {
        // only run with a ball, pokemon and the camera
        if (state.Ball != null && state.camera != null && state.Pokemon != null) {
            // the ball's offset from the camera
            Vector3 offset = new Vector3();

            Pose camPos = state.camera.getPose();

            // the camera's position as a Vector 3
            Vector3 camVec = new Vector3(camPos.tx(), camPos.ty(), camPos.tz());

            // the position of your finger, null if not touching
            Point pointer = state.getPointer();

            // get the cameras angle as a quaternion
            float[] rq = camPos.getRotationQuaternion();
            Quaternion q = new Quaternion(rq[0], rq[1], rq[2], rq[3]);

            switch (state.HoldingBall) {
                case 0: // no touch yet

                    // the ball hovers just in front the screen and in the lowest 3rd
                    offset.x = 0f;
                    offset.y = -0.1f;
                    offset.z = -0.25f;

                    // rotate the offset to the camera's angle with crazy math stuff
                    offset = Quaternion.rotateVector(q, offset);
                    // move the ball
                    state.Ball.setWorldPosition(Vector3.add(camVec, offset));

                    // if pointer isn't null, you are touching the screen, move to the next state.
                    if (pointer != null) {
                        state.HoldingBall = 1;
                    }

                    break;
                case 1: // holding

                    // if you aren't touching the screen move to the next state.
                    if (pointer == null) {
                        state.HoldingBall = 2;
                        break;
                    }

                    // set the ofset based your fingers position
                    offset.x = ((float) pointer.x / (float) state.displaySize.x - 0.5f) / 4f;
                    offset.y = ((float) pointer.y / (float) state.displaySize.y - 0.5f) / -4f;
                    offset.z = -0.25f;

                    // get the position of the ball 5 frames ago
                    Vector3 lastPos = trail[(frameNum - 5) % trailLen];
                    // if the position isn't null set the ball velocity to the ball's current
                    // position - the ball's position 5 frames ago. I needed to scale this down
                    // because it was too fast
                    if (lastPos != null) {
                        state.BallVelocity = Vector3.subtract(
                                state.Ball.getWorldPosition(),
                                lastPos
                        ).scaled(0.1f);
                    }

                    // rotate the offset with crazy math
                    offset = Quaternion.rotateVector(q, offset);
                    // move the ball
                    state.Ball.setWorldPosition(Vector3.add(camVec, offset));

                    break;
                case 2: // throwing

                    // add the acceleration to the ball.
                    state.BallVelocity = Vector3.add(state.BallVelocity, state.BallAcceleration);
                    // move the ball
                    state.Ball.setWorldPosition(
                            Vector3.add(state.Ball.getWorldPosition(), state.BallVelocity));

                    // if the ball is lower than the pokemon reset the state to holding the ball
                    if (state.Ball.getWorldPosition().y < state.Pokemon.getWorldPosition().y) {
                        state.HoldingBall = 0;
                        break;
                    }

                    // get the distance from the ball to the pokemon
                    float dist = Vector3.subtract(
                            // add half of the height of the objects to get the centers
                            Vector3.add(state.Ball.getWorldPosition(), new Vector3(0, 0.025f, 0)),
                            Vector3.add(state.Pokemon.getWorldPosition(), new Vector3(0, 0.1f, 0))
                    ).length();

                    // if the distance is less than 0.1 you have hit the pokemon and move to the
                    // final state
                    if (dist < 0.1) {
                        state.HoldingBall = 3;
                    }

                    break;
                case 3:

                    // change the state to holding
                    state.HoldingBall = 0;
                    // send a brodcast to delete the models, you can only remove nodes in the UI
                    // thread
                    bm.sendBroadcast(new Intent("adam.bibby.WIN"));
                    // sleep to make sure the broadcast has send and the nodes are deleted
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    return;


            }

            // set the trail's current index to the ball's position
            trail[frameNum % trailLen] = state.Ball.getWorldPosition();
        } else {
            trail[frameNum % trailLen] = null;
        }
    }
}
