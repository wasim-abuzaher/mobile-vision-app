package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {
    private static final float EYE_OPEN = 0.5f;
    private static final float SMILING = 0.15f;

    public static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap bitmap) {

        Bitmap resultBitmap = bitmap;
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(false)
                .build();

        Frame frame = new Frame.Builder()
                .setBitmap(bitmap).
                        build();

        SparseArray<Face> faces = detector.detect(frame);

        if(faces != null && faces.size() > 0) {
            Log.d("Emojifier", "Emojifier faces: " + faces.size());
            for(int i = 0; i < faces.size(); i++){
                Bitmap emojiBitmap;
                switch (whichEmoji(faces.valueAt(i))) {
                    case OPEN_SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                        break;
                    case CLOSED_SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
                        break;
                    case OPEN_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
                        break;
                    case CLOSED_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
                        break;
                    case LEFT_WINK_SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
                        break;
                    case LEFT_WINK_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
                        break;
                    case RIGHT_WINK_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
                        break;
                    default:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                        break;

                }
                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, faces.valueAt(i));
                Log.d("Emojifier", String.valueOf(whichEmoji(faces.valueAt(i))));
            }
        } else {
            Toast.makeText(context, "No faces detected!", Toast.LENGTH_SHORT).show();
        }


        detector.release();

        return resultBitmap;
    }

    private static Emoji whichEmoji(Face face) {
        boolean isLeftOpen = face.getIsLeftEyeOpenProbability() > EYE_OPEN;
        boolean isRightOpen = face.getIsRightEyeOpenProbability() > EYE_OPEN;
        boolean isSmiling = face.getIsSmilingProbability() > SMILING;

        Emoji emoji = Emoji.OPEN_SMILING;

        if(isLeftOpen && isRightOpen && isSmiling) {
            emoji = Emoji.OPEN_SMILING;
        } else if (isLeftOpen && isRightOpen && !isSmiling) {
            emoji = Emoji.OPEN_FROWNING;
        } else if (!isLeftOpen && !isRightOpen && isSmiling) {
            emoji = Emoji.CLOSED_SMILING;
        } else if (!isLeftOpen && !isRightOpen && !isSmiling) {
            emoji = Emoji.CLOSED_FROWNING;
        } else if (!isLeftOpen && isRightOpen && isSmiling) {
            emoji = Emoji.LEFT_WINK_SMILING;
        } else if (!isLeftOpen && isRightOpen && !isSmiling) {
            emoji = Emoji.LEFT_WINK_FROWNING;
        } else if (isLeftOpen && !isRightOpen && isSmiling) {
            emoji = Emoji.RIGHT_WINK_SMILING;
        } else if (isLeftOpen && !isRightOpen && !isSmiling) {
            emoji = Emoji.RIGHT_WINK_FROWNING;
        }

        return emoji;
    }

    /**
     * Combines the original picture with the emoji bitmaps
     *
     * @param backgroundBitmap The original picture
     * @param emojiBitmap      The chosen emoji
     * @param face             The detected face
     * @return The final bitmap, including the emojis over the faces
     */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = 1.0f;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

}

