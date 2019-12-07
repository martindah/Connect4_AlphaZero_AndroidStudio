package info.overrideandroid.connect4.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import info.overrideandroid.connect4.R;
import info.overrideandroid.connect4.ai.AiPlayer;
import info.overrideandroid.connect4.board.BoardLogic;
import info.overrideandroid.connect4.controller.GameMenuController;
import info.overrideandroid.connect4.controller.GamePlayController;
import info.overrideandroid.connect4.rules.GameRules;
import info.overrideandroid.connect4.view.BoardView;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


/**
 * Created by Martin Dahlkild & Yaxum Cedeno on 27/11/19.
 */

public class GamePlayActivity extends AppCompatActivity {

    static {
        System.loadLibrary("tensorflow_inference");
    }

    static public TensorFlowInferenceInterface tf;
    private GamePlayController mGameController;
    private final GameRules mGameRules = new GameRules();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        BoardView boardView = (BoardView) findViewById(R.id.gameView);
        mGameRules.importFrom(getIntent().getExtras());
        mGameController = new GamePlayController(this, boardView, mGameRules);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        if(mGameRules.getRule(GameRules.OPPONENT) == GameRules.Opponent.AI){
            tf = new TensorFlowInferenceInterface(getAssets(),AiPlayer.mPath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showAlert(R.string.back);
                break;
            case R.id.restart:
                showAlert(R.string.reset_game);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlert(final int msgId) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(msgId)
                .setCancelable(false)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (msgId == R.string.back) {
                            mGameController.exitGame();
                        } else {
                            mGameController.restartGame();
                        }
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        showAlert(R.string.back);
    }
}
