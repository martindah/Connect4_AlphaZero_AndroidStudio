package info.overrideandroid.connect4.ai;

import java.util.ArrayList;

import info.overrideandroid.connect4.controller.GamePlayController;

/**
 * Created by Martin Dahlkild & Yaxum Cedeno on 27/11/19.
 */



public class Node {

    int move;
    double Q;
    double N;
    double P;
    double W;
    Node parent;
    ArrayList<Node> children;
    int[] state;
    int finished = 0; //0 = not fininshed, otherwise equals winner
    int Player;

    public Node(Node parent, int[] state, int move, int Player) {
        this.parent = parent;
        this.state = state;
        this.move = move;
        this.Player = Player;
        children = new ArrayList<>();
    }
}
