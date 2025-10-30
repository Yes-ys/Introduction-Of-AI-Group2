// java
package stud.g01.problem.npuzzle;

public class ManualNPuzzleTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        try {
            testSolvableTrue();
            testSolvableFalse();
            testApplicableInsideBoard();
            testApplicableAtEdge();
            testApplicableTypeChecks();
        } catch (Throwable t) {
            fail("Unexpected exception: " + t);
        }
        System.out.printf("RESULT: passed=%d failed=%d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean cond, String msg) {
        if (cond) pass(msg); else fail(msg);
    }
    private static void assertFalse(boolean cond, String msg) {
        assertTrue(!cond, msg);
    }
    private static void pass(String msg) { passed++; System.out.println("OK: " + msg); }
    private static void fail(String msg) { failed++; System.out.println("FAIL: " + msg); }

    private static void testSolvableTrue() {
        int[][] board = {
                {1,2,3},
                {4,5,6},
                {7,8,0}
        };
        PuzzleBoard init = new PuzzleBoard(board);
        PuzzleBoard goal = new PuzzleBoard(board);
        NPuzzleProblem p = new NPuzzleProblem(init, goal);
        assertTrue(p.solvable(), "相同状态应可解");
    }

    private static void testSolvableFalse() {
        int[][] initArr = {
                {1,2,3},
                {4,5,6},
                {7,8,0}
        };
        int[][] goalArr = {
                {1,2,3},
                {4,5,6},
                {8,7,0}
        };
        PuzzleBoard init = new PuzzleBoard(initArr);
        PuzzleBoard goal = new PuzzleBoard(goalArr);
        NPuzzleProblem p = new NPuzzleProblem(init, goal);
        assertFalse(p.solvable(), "交换两个非空格数字应导致不可解");
    }

    private static void testApplicableInsideBoard() {
        int[][] board = {
                {1,2,3},
                {4,0,5},
                {6,7,8}
        };
        PuzzleBoard pb = new PuzzleBoard(board);
        NPuzzleProblem p = new NPuzzleProblem(pb, pb);
        assertTrue(p.applicable(pb, new Move(Direction.N)), "向上可行");
        assertTrue(p.applicable(pb, new Move(Direction.S)), "向下可行");
        assertTrue(p.applicable(pb, new Move(Direction.E)), "向右可行");
        assertTrue(p.applicable(pb, new Move(Direction.W)), "向左可行");
    }

    private static void testApplicableAtEdge() {
        int[][] board = {
                {0,1,2},
                {3,4,5},
                {6,7,8}
        };
        PuzzleBoard pb = new PuzzleBoard(board);
        NPuzzleProblem p = new NPuzzleProblem(pb, pb);
        assertFalse(p.applicable(pb, new Move(Direction.N)), "顶行不能再向上");
        assertFalse(p.applicable(pb, new Move(Direction.W)), "左列不能再向左");
        assertTrue(p.applicable(pb, new Move(Direction.S)), "向下可行");
        assertTrue(p.applicable(pb, new Move(Direction.E)), "向右可行");
    }

    private static void testApplicableTypeChecks() {
        int[][] board = {
                {1,2,3},
                {4,0,5},
                {6,7,8}
        };
        PuzzleBoard pb = new PuzzleBoard(board);
        NPuzzleProblem p = new NPuzzleProblem(pb, pb);
        assertFalse(p.applicable(null, new Move(Direction.N)), "null state 不可行");
        assertFalse(p.applicable(pb, null), "null action 不可行");
    }
}
