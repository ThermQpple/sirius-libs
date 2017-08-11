package de.unijena.bioinf.GibbsSampling.model;

import de.unijena.bioinf.ChemistryBase.algorithm.Scored;
import de.unijena.bioinf.GibbsSampling.model.ReactionStepSizeScorer.ConstantReactionStepSizeScorer;
import de.unijena.bioinf.GibbsSampling.model.scorer.ReactionScorer;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GibbsMFCorrectionNetwork<C extends Candidate<?>> {
    private static final double DEFAULT_CORRELATION_STEPSIZE = 10.0D;
    protected Graph<C> graph;
    private static final boolean iniAssignMostLikely = true;
    private int burnInRounds;
    private int currentRound;
    double[] priorProb;
    private int[] activeEdgeCounter;
    int[] activeIdx;
    boolean[] active;
    int[] overallAssignmentFreq;
    double[] assignmentFreqByPosterior;
    double[] maxPosteriorProbs;
    double[] posteriorProbs;
    double[] posteriorProbSums;
    private Random random;
    private final ExecutorService executorService;
    private final int numOfThreads;
    private final double pseudo;
    private final double logPseudo;

    public GibbsMFCorrectionNetwork(String[] ids, C[][] possibleFormulas, NodeScorer<C>[] nodeScorers, EdgeScorer<C>[] edgeScorers, EdgeFilter edgeFilter, int threads) {
        this.pseudo = 0.01D;
        this.logPseudo = Math.log(0.01D);

        for (Candidate[] pF : possibleFormulas) {
            if (pF==null || pF.length==0) throw new RuntimeException("some peaks don\'t have any explanation");
        }

        this.graph = buildGraph(ids, possibleFormulas, nodeScorers, edgeScorers, edgeFilter, threads);
        this.random = new Random();
        this.numOfThreads = threads;
        this.executorService = Executors.newFixedThreadPool(threads);
        this.setActive();

    }

    public GibbsMFCorrectionNetwork(String[] ids, C[][] possibleFormulas, Reaction[] reactions) {
        this(ids, possibleFormulas, new NodeScorer[]{new StandardNodeScorer()}, new EdgeScorer[]{new ReactionScorer(reactions, new ConstantReactionStepSizeScorer())}, new EdgeThresholdFilter(1.0D), 1);
    }

    public GibbsMFCorrectionNetwork(Graph graph, int threads) {
        this.pseudo = 0.01D;
        this.logPseudo = Math.log(0.01D);
        this.graph = graph;
        this.random = new Random();
        this.numOfThreads = threads;
        this.executorService = Executors.newFixedThreadPool(threads);
        this.setActive();
    }

    public void shutdown() {
        this.executorService.shutdown();
    }

    private void futuresGet(Iterable<Future> futures){
        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <C extends Candidate<?>> Graph<C> buildGraph(String[] ids, C[][] possibleFormulas, NodeScorer<C>[] nodeScorers, EdgeScorer<C>[] edgeScorers, EdgeFilter edgeFilter, int numOfThreads) {
        for (NodeScorer<C> nodeScorer : nodeScorers) {
            nodeScorer.score(possibleFormulas);
        }

        List<String> newIds = new ArrayList();
        List<Scored<C>[]> newFormulas = new ArrayList();

        for(int i = 0; i < possibleFormulas.length; ++i) {
            C[] candidates = possibleFormulas[i];
            String id = ids[i];
            ArrayList<Scored<Candidate>> scoredCandidates = new ArrayList();

            for (C candidate : candidates) {
                scoredCandidates.add(new Scored(candidate, candidate.getNodeLogProb()));
            }

            if(scoredCandidates.size() > 0) {
                newIds.add(id);
                newFormulas.add(scoredCandidates.toArray(new Scored[0]));
            }
        }

        String[] filteredIds = newIds.toArray(new String[0]);
        Scored<C>[][] scoredPossibleFormulas = newFormulas.toArray(new Scored[0][]);
        Graph<C> graph = new Graph<C>(filteredIds, scoredPossibleFormulas);
        graph.init(edgeScorers, edgeFilter, numOfThreads);
        return graph;
    }

    private void setActive() {
//        System.out.println("setActive");
        this.priorProb = new double[this.graph.getSize()];
        this.activeEdgeCounter = new int[this.graph.getSize()];
        this.activeIdx = new int[this.graph.numberOfCompounds()];
        this.active = new boolean[this.graph.getSize()];
        int z = 0;

        for(int i = 0; i < this.graph.numberOfCompounds(); ++i) {
            Scored[] possibleFormulasArray = this.graph.getPossibleFormulas(i);
            int idx = Integer.MIN_VALUE;
            //set best explanation active
            if (iniAssignMostLikely){
                double maxScore = Double.NEGATIVE_INFINITY;
                for (int j = 0; j < possibleFormulasArray.length; j++) {
                    double score = possibleFormulasArray[j].getScore();
                    if (score>maxScore){
                        maxScore = score;
                        idx = j;
                    }
                }
            } else {
                //sample
//                double[] scores = new double[possibleFormulasArray.length];
//                double sum = 0;
//                for (int j = 0; j < possibleFormulasArray.length; j++) {
//                    double score = possibleFormulasArray[j].getScore();
//                    scores[j] = score;
//                    sum += score;
//                }
//                idx = getRandomIdx(0, scores.length-1, sum, scores);
//

                double[] scores = new double[possibleFormulasArray.length];
                double sum = 0;
                double maxLog = Double.NEGATIVE_INFINITY;
                for (int j = 0; j < possibleFormulasArray.length; j++) {
                    double score = possibleFormulasArray[j].getScore();
                    if (score>maxLog) maxLog = score;
                }

                for (int j = 0; j < possibleFormulasArray.length; j++) {
                    final double score = Math.exp(possibleFormulasArray[j].getScore()-maxLog);
                    scores[j] = score;
                    sum += score;
                }
                assert sum > 0.0D;

                idx = getRandomIdx(0, scores.length-1, sum, scores);
            }

            activeIdx[i] = idx;
            active[idx+z] = true;
            z+=possibleFormulasArray.length;
        }

        ///set priorProb and maxPriorProb
        for(int i = 0; i < this.priorProb.length; ++i) {
            int[] conn = this.graph.getConnections(i);

            for(int j = 0; j < conn.length; ++j) {
                if(this.active[conn[j]]) {
                    this.addActiveEdge(conn[j], i);
                    ++this.activeEdgeCounter[i];
                }
            }

            this.priorProb[i] += (double)(this.graph.numberOfCompounds() - this.activeEdgeCounter[i] - 1) * this.logPseudo;
        }

        this.posteriorProbs = new double[this.graph.getSize()];
        this.posteriorProbSums = new double[this.graph.numberOfCompounds()];
        //set posteriorProbs
        for(int i = 0; i < this.graph.numberOfCompounds(); ++i) {
            this.updatePeak(i);
        }

        this.overallAssignmentFreq = new int[this.graph.getSize()];
        this.assignmentFreqByPosterior = new double[this.graph.getSize()];
        this.maxPosteriorProbs = new double[this.graph.getSize()];
    }

    private double getPosteriorScore(double prior, double score) {
        return prior + score;
    }

    public void iteration(int maxSteps) {
        this.iteration(maxSteps, maxSteps / 5);
    }

    public void iteration(int maxSteps, int burnIn) {
        this.burnInRounds = burnIn;
        int iterationStepLength = this.graph.numberOfCompounds();
        long startTime = System.nanoTime();

        int step = (burnIn + maxSteps)/10;

        for(int i = 0; i < burnIn + maxSteps; ++i) {
            this.currentRound = i;
            boolean changed = false;
            int[] randomOrdering = getRandomOrdering(iterationStepLength);

            for(int runtime = 0; runtime < randomOrdering.length; ++runtime) {
                if(this.iterationStep(randomOrdering[runtime])) {
                    changed = true;
                }
            }

            if((i % step == 0 && i>0) || i == (burnIn+maxSteps-1)) {
                long var11 = System.nanoTime() - startTime;
//                System.out.println("runtime in ms: " + var11 / 1000000L);
                System.out.println("step "+((double)(((i+1)*100/(maxSteps+burnIn))))+"%");
            }
        }

    }

    public String[] getIds() {
        return this.graph.getIds();
    }

    public Scored<C>[][] getAllPossibleMolecularFormulas() {
        return this.graph.getPossibleFormulas();
    }

    public Scored<C>[][] getAllEdges() {
        ArrayList edgeList = new ArrayList();

        for(int i = 0; i < this.graph.getSize(); ++i) {
            int[] currentConnections = this.graph.getConnections(i);
            Scored mf1 = this.graph.getPossibleFormulas1D(i);

            for(int j = 0; j < currentConnections.length; ++j) {
                int c = currentConnections[j];
                if(c <= i) {
                    Scored mf2 = this.graph.getPossibleFormulas1D(c);
                    edgeList.add(new Scored[]{mf1, mf2});
                }
            }
        }

        return (Scored[][])edgeList.toArray(new Scored[0][]);
    }

    public int[][] getAllEdgesIndices() {
        return this.graph.getAllEdgesIndices();
    }

    private Scored<C>[][] getFormulasSortedByScoring(double[] scoring) {
        Scored<C>[][] candidatesByCompound = new Scored[this.graph.numberOfCompounds()][];

        for(int i = 0; i < this.graph.numberOfCompounds(); ++i) {
            int[] b = this.graph.getPeakBoundaries(i);
            int min = b[0];
            int max = b[1];
            Scored<C>[] candidates = new Scored[max - min + 1];
            double sum = 0.0D;

            int j;
            double freq;
            for(j = min; j <= max; ++j) {
                freq = scoring[j];
                sum += freq;
            }

            for(j = min; j <= max; ++j) {
                freq = scoring[j];
                candidates[j - min] = new Scored<>(this.graph.getPossibleFormulas1D(j).getCandidate(), freq / sum);
            }

            Arrays.sort(candidates, Scored.<C>desc());
            candidatesByCompound[i] = candidates;
        }

        return candidatesByCompound;
    }

    private Scored<C>[][] getFormulasSortedByScoring(int[] scoring) {
        Scored<C>[][] candidatesByCompound = new Scored[this.graph.numberOfCompounds()][];

        for(int i = 0; i < this.graph.numberOfCompounds(); ++i) {
            int[] b = this.graph.getPeakBoundaries(i);
            int min = b[0];
            int max = b[1];
            Scored<C>[] candidates = new Scored[max - min + 1];
            int sum = 0;

            int j;
            int freq;
            for(j = min; j <= max; ++j) {
                freq = scoring[j];
                sum += freq;
            }

            for(j = min; j <= max; ++j) {
                freq = scoring[j];
                candidates[j - min] = new Scored<>(this.graph.getPossibleFormulas1D(j).getCandidate(), 1.0D * (double)freq / (double)sum);
            }

            Arrays.sort(candidates, Scored.<C>desc());
            candidatesByCompound[i] = candidates;
        }

        return candidatesByCompound;
    }

    public EdgeScorer[] getEdgeScorers() {
        return this.graph.getUsedEdgeScorers();
    }

    public Graph getGraph() {
        return this.graph;
    }

    public Scored<C>[][] getChosenFormulasByMaxPosterior() {
        return this.getFormulasSortedByScoring(this.maxPosteriorProbs);
    }

    public Scored<C>[][] getChosenFormulasBySampling() {
        return this.getFormulasSortedByScoring(this.overallAssignmentFreq);
    }

    public Scored<C>[][] getChosenFormulasByAddedUpPosterior() {
        return this.getFormulasSortedByScoring(this.assignmentFreqByPosterior);
    }

    public Scored<C>[][] getChosenFormulas() {
        return this.getFormulasSortedByScoring(this.overallAssignmentFreq);
    }

    private boolean iterationStep(int peakIdx) {
        int[] b = this.graph.getPeakBoundaries(peakIdx);
        int min = b[0];
        int max = b[1];
        double probSum = this.posteriorProbSums[peakIdx];
        int absIdx = this.getRandomIdx(min, max, probSum, this.posteriorProbs);
        if(this.currentRound > this.burnInRounds) {
            double absCurrentActive;
            if((double)(this.currentRound - this.burnInRounds) % DEFAULT_CORRELATION_STEPSIZE == 0.0D) {
                ++this.overallAssignmentFreq[absIdx];
                for(int i = min; i <= max; ++i) {
                    absCurrentActive = this.posteriorProbs[i];
                    this.assignmentFreqByPosterior[i] += absCurrentActive;
                }
            }
            for(int i = min; i <= max; ++i) {
                absCurrentActive = this.posteriorProbs[i];
                if(this.maxPosteriorProbs[i] < absCurrentActive) {
                    this.maxPosteriorProbs[i] = absCurrentActive;
                }
            }
        }

        int relCurrentActive = this.activeIdx[peakIdx];
        int absCurrentActive = relCurrentActive + min;
        int relIndex = absIdx - min;
        if(relCurrentActive == relIndex) {
            return false;
        } else {
            BitSet toUpdate = new BitSet();
            int[] c = this.graph.getConnections(absCurrentActive);
            for (int conjugate : c) {
                this.removeActiveEdge(absCurrentActive, conjugate);
                final int corrspondingPeakIdx = this.graph.getPeakIdx(conjugate);
                toUpdate.set(corrspondingPeakIdx);
            }

            c = this.graph.getConnections(absIdx);
            for (int conjugate : c) {
                this.addActiveEdge(absIdx, conjugate);
                final int corrspondingPeakIdx = this.graph.getPeakIdx(conjugate);
                toUpdate.set(corrspondingPeakIdx);
            }


            for (int i = toUpdate.nextSetBit(0); i >= 0; i = toUpdate.nextSetBit(i+1)) {
                updatePeak(i);
                if (i == Integer.MAX_VALUE) {
                    break; // or (i+1) would overflow
                }
            }

            this.activeIdx[peakIdx] = relIndex;
            this.active[absCurrentActive] = false;
            this.active[absIdx] = true;
            return true;
        }
    }

    private void removeActiveEdge(int outgoing, int incoming) {
        this.priorProb[incoming] -= this.graph.getLogWeight(outgoing, incoming);
        this.priorProb[incoming] += this.logPseudo;
    }

    private void addActiveEdge(int outgoing, int incoming) {
        this.priorProb[incoming] += this.graph.getLogWeight(outgoing, incoming);
        this.priorProb[incoming] -= this.logPseudo;
    }

    /**
     *
     * @param minIdx
     * @param maxIdx
     * @param probSum
     * @param probs
     * @return absolute index
     */
    private int getRandomIdx(int minIdx, int maxIdx, double probSum, double[] probs){
        double r = random.nextDouble()*probSum;
        int absIdx = minIdx-1;
        double sum = 0;

        try {
            do {
                absIdx++;
                sum += probs[absIdx];
            } while (sum<r);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("sum "+sum);
            System.err.println("min "+maxIdx+" max "+maxIdx+" absIdx "+absIdx+" "+Arrays.toString(Arrays.copyOfRange(probs, minIdx, maxIdx+1)));
            System.err.println("probsum "+probSum+" sum "+sum+" r "+r);
        }

        if (absIdx>maxIdx) throw new RuntimeException("sampling by probability produced error");

        return absIdx;
    }

    private void updatePeak(int peakIdx) {
        int[] b = this.graph.getPeakBoundaries(peakIdx);
        int min = b[0];
        int max = b[1];
        double maxLog = Double.NEGATIVE_INFINITY;

        for(int i = min; i <= max; ++i) {
            this.posteriorProbs[i] = this.getPosteriorScore(this.priorProb[i], this.graph.getCandidateScore(i));
            if(this.posteriorProbs[i] > maxLog) {
                maxLog = this.posteriorProbs[i];
            }
        }

        double sum = 0.0D;

        for(int i = min; i <= max; ++i) {
            this.posteriorProbs[i] = Math.exp(this.posteriorProbs[i] - maxLog);
            sum += this.posteriorProbs[i];
        }

        assert sum > 0.0D;

        this.posteriorProbSums[peakIdx] = sum;
    }

    /**
     *
     * @param max 0..max , max exclusive
     * @return
     */
    public static int[] getRandomOrdering(int max){
        return getRandomOrdering(0, max);
    }

    /**
     * min ... max, max exclusive
     * @param min
     * @param max
     * @return
     */
    public static int[] getRandomOrdering(int min, int max) {
        TIntArrayList numbers = new TIntArrayList(max - min);
        TIntArrayList ordering = new TIntArrayList(max - min);
        Random random = new Random();

        for(int i = min; i < max; ++i) {
            numbers.add(i);
        }

        while(numbers.size() > 0) {
            final int pos = random.nextInt(numbers.size());
            ordering.add(numbers.removeAt(pos));
        }

        return ordering.toArray();
    }

}