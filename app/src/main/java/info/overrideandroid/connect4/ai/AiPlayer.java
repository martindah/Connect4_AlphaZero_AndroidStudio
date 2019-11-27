package info.overrideandroid.connect4.ai;

import info.overrideandroid.connect4.board.BoardLogic;
import info.overrideandroid.connect4.controller.GamePlayController;
import info.overrideandroid.connect4.activity.GamePlayActivity;
import java.lang.Math;

/**
 * Created by Martin Dahlkild & Yaxum Cedeno on 27/11/19.
 */

public class AiPlayer {

    private final BoardLogic mBoardLogic;
    public static String mPath;
    private int[] LastBoard;
    private static final String TAG = GamePlayController.class.getName();

    public AiPlayer(BoardLogic boardLogic) {
        mBoardLogic = boardLogic;
    }


    static {
        System.loadLibrary("tensorflow_inference");
    }

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    private static String INPUT_NAME = "main_input";
    private static String OUTPUT_NAME_1 = "value_head/Tanh";
    private static String OUTPUT_NAME_2 = "policy_head/MatMul";

    private static int[] ArrayToList(int[][] arr)
    {
        int[] new_arr;
        new_arr = new int[42];
        for(int i = 0; i < 6; i++)
        {
            for(int j = 0; j < 7; j++)
            {
                new_arr[j + 7*i] = arr[i][j];
                if(new_arr[j + 7*i] == 2)
                {
                    new_arr[j + 7*i] = -1;
                }
            }
        }
        return new_arr;
    }

    public static int[][] ListToArray(int[] arr)
    {
        int[][] new_arr;
        new_arr = new int[6][7];
        for(int i = 0; i < 6; i++)
        {
            for(int j = 0; j < 7; j++)
            {
                new_arr[i][j] = arr[j + 7*i];
                if(new_arr[i][j] == -1)
                {
                    new_arr[i][j] = 2;
                }
            }
        }
        return new_arr;
    }

    private static float[] Input_Array(int[] state, int Player)
    {
        float[] input_array;
        input_array = new float[6*7*2];
        for(int i = 0; i < state.length; i++)
        {
            if(state[i] == -Player)
            {
                input_array[state.length + i] = 1;
            }else if(state[i] == Player)
            {
                input_array[i] = 1;
            }
        }
        return input_array;
    }

    public void setDifficulty(int depth) {
        if(depth == 0)
        {
            mPath = "tf_model05.pb";
        }
        else if(depth == 1)
        {
            mPath = "tf_model20.pb";
        }
        else if(depth == 2)
        {
            mPath = "tf_model42.pb";
        }
    }
    /**
     * run ai move
     * @return column to put AI disc
     */

    public int getColumn() {
        double[] pi = MCTS.simulate(ArrayToList(GamePlayController.mGrid));

        Object[] result = argmax(pi);
        int best_move = (int) result[0];
        return best_move % 7;
    }


    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    private static float[] PREDICTIONS = new float[42];
    private static float[] VALUE = new float[42];
    private float[] floatValues;
    private int[] INPUT_SIZE = {2,6,7};


    public static int[] AllowedActions(int[] board)
    {
        int[] allowed;
        allowed = new int[42];
        for(int i = 0; i < board.length; i++)
        {
            if( i >= board.length - 7) {
                if (board[i] == 0) {
                    allowed[i] = i+1;
                }
            }
                else
                {
                    if(board[i] == 0 & board[i+7] != 0)
                    {
                        allowed[i] = i+1;
                    }
                }
        }
        return allowed;
    }

    //FUNCTION TO COMPUTE THE MAXIMUM PREDICTION AND ITS CONFIDENCE
    private Object[] argmax(double[] array){

        int best = -1;
        //float probs;
        double best_confidence = -10.0f;
        for(int i = 0;i < array.length;i++){

            double value = array[i];

            if (value > best_confidence){
                best_confidence = value;
                best = i;
            }
        }

        return new Object[]{best,best_confidence};

    }


    public static float[] predict(int[] Board, int Player){
                //Pass input into the tensorflow
                float[] input_array = Input_Array(Board, Player);
                GamePlayActivity.tf.feed(INPUT_NAME, input_array,1,2,6,7);

                //compute predictions
                GamePlayActivity.tf.run(new String[]{OUTPUT_NAME_1, OUTPUT_NAME_2});

                //copy the output into the PREDICTIONS array
                GamePlayActivity.tf.fetch(OUTPUT_NAME_1,VALUE);
                GamePlayActivity.tf.fetch(OUTPUT_NAME_2,PREDICTIONS);

                int[] allowed = AllowedActions(Board);
                float sum = 0;
                float[] probs = new float[43];
                for(int i = 0; i < PREDICTIONS.length; i++)
                {
                   if(allowed[i] == 0) {
                       sum = sum + (float) Math.exp(-100);
                   }
                   else{
                       sum = sum + (float) Math.exp(PREDICTIONS[i]);
                   }
                }
                for(int i = 0; i < PREDICTIONS.length; i++) {

                    float value = PREDICTIONS[i];
                    double odds = Math.exp(value);
                    probs[i] = (float) odds/sum;
                }
                probs[42] = VALUE[0];
                return probs;
    }
}
