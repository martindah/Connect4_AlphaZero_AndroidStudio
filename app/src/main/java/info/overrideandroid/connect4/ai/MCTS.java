package info.overrideandroid.connect4.ai;
import android.support.annotation.NonNull;
import info.overrideandroid.connect4.board.BoardLogic;
import info.overrideandroid.connect4.controller.GamePlayController;
import umontreal.ssj.probdist.GammaDist;


/**
 * Created by Martin Dahlkild & Yaxum Cedeno on 27/11/19.
 */



public class MCTS{
    static Node start;
    private static int[] mFree = new int[7];
    private static int[] breadcrumbs;
    private static int[][] currentstate = new int[6][7];
    private static BoardLogic mBoardLogic = new BoardLogic(currentstate, mFree);
    @NonNull
    private static BoardLogic.Outcome mOutcome = BoardLogic.Outcome.NOTHING;

    private static void buildMCTS(int[] state){
        Node s = start;
        if(s != null){
            for(Node n: s.children){
                if(n.Player == state[n.move]){
                    if(n.children != null)
                    {
                        for(Node c : n.children)
                        {
                            if(state[c.move] == c.Player)
                            {
                                start = c;
                                start.Player = -start.Player;
                                return;
                            }
                        }
                    }
                }
            }
        }
        NewNode(state);
    }

    private static void NewNode(int[] state){
        int turn = 1;
        if (GamePlayController.mPlayerTurn != 1) {
            turn = -1;
        }
        start = new Node(null, state, (byte) -1, turn);
    }

    private static void backFill(float value, int currentPlayer) {
        Node s = start;
        int direction = 0;
        int turn = s.Player;
        for (int action : breadcrumbs) {
            if (action == 0) {
                break;
            }
            for (Node n : s.children) {
                n.Player = turn;
                if (n.move == action - 1) {
                    if (currentPlayer == n.Player) {
                        direction = 1;
                    } else {
                        direction = -1;
                    }
                    n.N = n.N + 1;
                    n.W = n.W + value * direction; //value
                    n.Q = n.W / n.N;
                    s = n;
                    break;
                }
            }
            turn = -turn;
        }
    }

    private static Node selection() { //select a leaf node return index
        double epsilon = 0;
        double cpuct = 1;
        double alpha = 0.8;
        double[] nu = new double[7];
        double[] y = new double[7];
        double Q = 0;
        double U = 0;
        int first = 1;
        int count = 0;
        breadcrumbs = new int[42];
        int simulationAction = 0;
        Node s = start;
        Node simulationEdge = start;
        Node sim = s;
        while (sim.children.size() != 0) {
            double maxQU = -9999;
            if (first == 1) {
                double sum = 0;
                for (int i = 0; i < sim.children.size(); i++) {
                    epsilon = 0.2;
                    y[i] = GammaDist.inverseF(alpha, 1.0, 1, Math.random());
                    sum += y[i];
                }
                for(int i = 0; i < y.length; i++){
                    nu[i] = y[i]/sum;
                }
            } else {
                for (int i = 0; i < sim.children.size(); i++) {
                    epsilon = 0;
                    nu[i] = 0;
                }
            }
            double Nb = 0;
            for (Node child : sim.children) {
                Nb = Nb + child.N;
            }
            double maxP = 0;
            for (int j = 0; j < sim.children.size(); j++) {
                Node n = sim.children.get(j);

                U = cpuct * ((1 - epsilon) * n.P + epsilon * nu[j]) * Math.sqrt(Nb) / (1 + n.N);
                Q = n.Q;
                if(Q + U > maxQU) {//(n.P > maxP){
                    maxQU = Q + U;
                    maxP = n.P;
                    simulationAction = n.move;
                    simulationEdge = n;
                }
            }
            simulationEdge.state[simulationAction] = simulationEdge.Player;
            simulationEdge.Player = -simulationEdge.Player;
            first = 0;
            sim = simulationEdge;
            breadcrumbs[count] = simulationAction + 1;
            count += 1;
        }
        return sim;
    }

    private static float expansion(Node n) { // choose child of leaf node
        float value = -1;
        currentstate = AiPlayer.ListToArray(n.state);
        mOutcome = mBoardLogic.checkWin(currentstate);
        if(mOutcome != BoardLogic.Outcome.NOTHING)
        {
            return value;
        }
        else {
            int[] moves = AiPlayer.AllowedActions(n.state);
            float[] probs = AiPlayer.predict(n.state, n.Player);
            Node c = null;
            for (int m = 0; m < moves.length; m++) {
                if (moves[m] > 0) {
                    int[] state = new int[42];
                    for (int i = 0; i < n.state.length; i++) {
                        state[i] = n.state[i];
                    }
                    state[moves[m]-1] = n.Player;

                    c = new Node(n, state, moves[m]-1, n.Player);
                    c.P = probs[m]; //probability for the move from model
                    n.children.add(c);
                    //nodes.add(c);
                }
            }
            return probs[42];
        }
    }

    private static double[] getAV(){
        Node s = start;
        double sum = 0;
        double[] pi = new double[42];
        //double[] values = new double[42];
        for(Node n: s.children)
        {
            pi[n.move] = n.N;
            //values[n.move] = n.Q;
            System.out.print(n.W + ", " + n.Q + ", " + n.N + ", ");
        }
        System.out.println(" ");
        for(int i = 0;i < pi.length; i++)
        {
            sum += pi[i];
        }
        for(int i = 0;i < pi.length; i++)
        {
            pi[i] = pi[i]/(sum + 1);
        }
        System.out.println();
        for (int i = 0; i <= 5; ++i) {
            for (int j = 0; j <= 6; ++j) {
                System.out.print(pi[j + 7*i] + " ");
            }
            System.out.println();
        }
        System.out.println();
        return pi;
    }
    public static double[] simulate(int[] state){
        buildMCTS(state);
        for(int i = 0; i < 50; i++)
        {
            Node n = selection();

            float value = expansion(n);

            backFill(value, n.Player);
        }
        return getAV();
    }
}

