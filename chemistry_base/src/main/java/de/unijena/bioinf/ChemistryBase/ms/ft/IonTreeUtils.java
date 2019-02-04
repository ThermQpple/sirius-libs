package de.unijena.bioinf.ChemistryBase.ms.ft;

import de.unijena.bioinf.ChemistryBase.chem.Ionization;
import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.chem.PeriodicTable;
import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.ms.AnnotatedPeak;
import de.unijena.bioinf.graphUtils.tree.PostOrderTraversal;
import de.unijena.bioinf.ms.annotations.TreeAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *     //todo this whole class must be adjusted to adduct switches
 * Applies all in-source fragmentations and adducts to the tree. Set a PrecursorIonType annotation object to each vertex
 * of the tree
 */
public class IonTreeUtils {
    public enum Type implements TreeAnnotation  {RAW, RESOLVED, IONIZED}

    public boolean isResolvable(FTree tree, PrecursorIonType ionType) {
        return (tree.getAnnotationOrThrow(PrecursorIonType.class).getIonization().equals(ionType.getIonization()) && tree.getRoot().getFormula().isSubtractable(ionType.getAdduct()));
    }

    /**
     * Takes a computed tree as input. Creates a copy of this tree and resolve the PrecursorIonType, such that insource
     * fragmentations are reflected in the tree and all vertices in the tree have a neutral molecular formula excluding
     * the adduct and ionization. Annotates each vertex with a PrecursorIonType.
     */
    public FTree treeToNeutralTree(FTree tree, PrecursorIonType ionType) {
        if (tree.getAnnotationOrNull(Type.class) == Type.RESOLVED) {
            throw new IllegalArgumentException("Cannot neutralize an already neutral tree");
        }
        if (!tree.getAnnotationOrThrow(PrecursorIonType.class).getIonization().equals(ionType.getIonization())) {
            throw new IllegalArgumentException("Precursor Ion Type '" + ionType.toString() + "' does not match tree with ionization '" + tree.getAnnotationOrThrow(PrecursorIonType.class).toString() + "'");
        }
        tree = new FTree(tree);
        tree.setAnnotation(PrecursorIonType.class, ionType);
        return treeToNeutralTree(tree);
    }

    /**
     * Takes a computed tree as input with a certain PrecursorIonType. Resolve the PrecursorIonType, such that insource
     * fragmentations are reflected in the tree and all vertices in the tree have a neutral molecular formula excluding
     * the adduct and ionization. Annotates each vertex with a PrecursorIonType.
     *
     * This modifications might be in-place. So the caller have to ensure to copy the tree if he do not want to change it.
     */
    public FTree treeToNeutralTree(FTree tree) {
        if (tree.getAnnotationOrNull(Type.class)==Type.RESOLVED) {
            return tree;
        } else if (tree.getAnnotationOrNull(Type.class) == Type.IONIZED) {
            throw new IllegalArgumentException("Cannot neutralize ionized tree.");
        }
        PrecursorIonType ion = tree.getAnnotationOrThrow(PrecursorIonType.class);

        if (ion.getInSourceFragmentation().atomCount() > 0) {
            // add the in-source fragmentation to the tree
            tree.addRoot(ion.getInSourceFragmentation().add(tree.getRoot().getFormula()), ion.getIonization());
            tree.getOrCreateLossAnnotation(InsourceFragmentation.class).set(tree.getRoot().getOutgoingEdge(0), new InsourceFragmentation(true));
            ion = ion.withoutInsource();
        }
        if (ion.getAdduct().atomCount() > 0) {
            // remove the adduct from all fragments
            reduceTree(tree, ion, ion.getAdduct());
        } else {
            setIonizationAsPrecursorIonTypeToEachNode(tree, tree.getRoot());
        }

        // check
        {
            final FragmentAnnotation<PrecursorIonType> ionTypeF = tree.getFragmentAnnotationOrNull(PrecursorIonType.class);
            for (Fragment f : tree) {
                if (!ionTypeF.get(f).getIonization().equals(f.getIonization())) {
                    LoggerFactory.getLogger(this.getClass()).error("Error: " + f.getFormula() + " has ion type " + ionTypeF.get(f));
                }
            }
        }
        tree.setAnnotation(Type.class, Type.RESOLVED);
        return tree;
    }

    /**
     * Insert the ionization into every vertex, such that each vertex is ionized with [M+?]+ and is labeled with the
     * molecular formula of the ion. Resolve also insource fragmentations.
     *
     * This modifications might be in-place. So the caller have to ensure to copy the tree if he do not want to change it.
     */
    public FTree treeToIonTree(FTree tree) {
        if (tree.getAnnotationOrNull(Type.class)==Type.IONIZED) {
            return tree;
        } else if (tree.getAnnotationOrNull(Type.class) == Type.RESOLVED) {
            throw new IllegalArgumentException("Cannot neutralize ionized tree.");
        }
        PrecursorIonType ion = tree.getAnnotationOrThrow(PrecursorIonType.class);
        final FragmentAnnotation<PrecursorIonType> fa = tree.getOrCreateFragmentAnnotation(PrecursorIonType.class);
        if (ion.getInSourceFragmentation().atomCount() > 0) {
            // add the in-source fragmentation to the tree
            tree.addRoot(ion.getInSourceFragmentation().add(tree.getRoot().getFormula()), ion.getIonization());
            tree.getOrCreateLossAnnotation(InsourceFragmentation.class).set(tree.getRoot().getOutgoingEdge(0), new InsourceFragmentation(true));
            ion = ion.withoutInsource();
        }
        final PrecursorIonType empty = PeriodicTable.getInstance().getUnknownPrecursorIonType(ion.getCharge());
        for (Fragment f : tree) {
            //todo adduct-switch: still same ionization? use PrecursorIonType?
            f.setFormula(ion.neutralMoleculeToPrecursorIon(f.getFormula()), f.getIonization());
            fa.set(f, empty);
        }
        tree.setAnnotation(PrecursorIonType.class, empty);
        tree.setAnnotation(Type.class, Type.IONIZED);
        return tree;
    }

    private void reduceTree(final FTree tree, PrecursorIonType iontype, final MolecularFormula adduct) {

        final Score.ScoreAdder scoreAdd = Score.extendWith("adductSubstitution");
        reduceSubtree(tree, iontype, adduct, tree.getRoot(), scoreAdd);
    }

    private void reduceSubtree(final FTree tree, PrecursorIonType iontype, final MolecularFormula adduct, Fragment vertex, Score.ScoreAdder adder) {
        final FragmentAnnotation<PrecursorIonType> fa = tree.getOrCreateFragmentAnnotation(PrecursorIonType.class);
        final FragmentAnnotation<AnnotatedPeak> peakAno = tree.getOrCreateFragmentAnnotation(AnnotatedPeak.class);
        final LossAnnotation<Score> lossAno = tree.getLossAnnotationOrNull(Score.class);


        //todo can there arise problems with PrecursorIonType.SPECIAL_TYPES?
//        final PrecursorIonType newIonType = PrecursorIonType.getPrecursorIonType(vertex.getIonization()).substituteInsource(iontype.getInSourceFragmentation());
        final PrecursorIonType newIonType = PrecursorIonType.getPrecursorIonType(vertex.getIonization()).substituteInsource(iontype.getInSourceFragmentation());
        // if adduct is lost: contract loss
        if (vertex.getInDegree() > 0 && vertex.getIncomingEdge().getFormula().equals(adduct)) {
//            final PrecursorIonType newIonType = iontype.withoutAdduct();
            // delete this vertex
            final List<Fragment> children = new ArrayList<Fragment>(vertex.getChildren());
            for (Fragment f : children) {
                Loss oldLoss = f.getIncomingEdge();
                Loss contractedLoss = vertex.getIncomingEdge();
                Loss newLoss = tree.swapLoss(f, vertex.getParent());
                if (lossAno!=null) {
                    final Score oldScore = lossAno.get(oldLoss);
                    final Score contrScore = lossAno.get(contractedLoss);
                    if (oldScore != null && contrScore!=null) {;
                        lossAno.set(f.getIncomingEdge(), adder.add(oldScore, contrScore.sum()));
                    } else if (oldScore!=null) {
                        lossAno.set(f.getIncomingEdge(), oldScore);
                    }
                }
            }
            tree.deleteVertex(vertex);
            fa.set(vertex, newIonType);
            for (Fragment f : children) {
//                setPrecursorToEachNode(tree, f, newIonType);
                setAdductAndInsourceToEachNode(tree, f, newIonType);
            }
            // finished
            return;
        }
        // if adduct is still part of the fragment: remove it from fragment
        final MolecularFormula f = vertex.getFormula().subtract(adduct);
        if (f.isAllPositiveOrZero() && !f.isEmpty()) {
            vertex.setFormula(f, vertex.getIonization());
            if (peakAno.get(vertex)!=null)
                peakAno.set(vertex, peakAno.get(vertex).withFormula(f));
//            fa.set(vertex, iontype.withoutAdduct());
            fa.set(vertex, newIonType);
            final ArrayList<Fragment> childs = new ArrayList<Fragment>(vertex.getChildren());
            for (Fragment g : childs) {
                reduceSubtree(tree, iontype, adduct, g, adder);
            }
        } else if (vertex.getInDegree()>0){
            // if adduct is part of the loss, remove it from loss
            final MolecularFormula l = vertex.getIncomingEdge().getFormula().subtract(adduct);
            if (l.isAllPositiveOrZero()) {
                vertex.getIncomingEdge().setFormula(l);
//                setPrecursorToEachNode(tree, vertex, iontype.withoutAdduct());
                setAdductAndInsourceToEachNode(tree, vertex, iontype.withoutAdduct());
            } else {
                // otherwise: delete whole subtree
                tree.deleteSubtree(vertex);
            }
        } else {
            logger.warn("Cannot remove adduct from ion formula: " + vertex.getFormula() + " with adduct " + iontype.toString() + " in tree " + tree.getRoot().getFormula());
        }
    }
    protected static Logger logger = LoggerFactory.getLogger(IonTreeUtils.class);

    private void setPrecursorToEachNode(FTree tree, Fragment f, final PrecursorIonType ionType) {
        //todo do we need both? Ionization and PrecursorIonType?
        final FragmentAnnotation<PrecursorIonType> fa = tree.getOrCreateFragmentAnnotation(PrecursorIonType.class);
        final FragmentAnnotation<Ionization> ia = tree.getOrCreateFragmentAnnotation(Ionization.class);
        new PostOrderTraversal<Fragment>(tree.getCursor(f)).run(new PostOrderTraversal.Run<Fragment>() {
            @Override
            public void run(Fragment vertex, boolean isRoot) {
                fa.set(vertex, ionType);
                ia.set(vertex,ionType.getIonization());
            }
        });
    }


    /**
     * this only sets adduct and insource, but does not change ionization of fragment.
     * @param tree
     * @param f
     * @param ionType
     */
    private void setAdductAndInsourceToEachNode(FTree tree, Fragment f, final PrecursorIonType ionType) {
        //todo do we need both? Ionization and PrecursorIonType?
        final FragmentAnnotation<PrecursorIonType> fa = tree.getOrCreateFragmentAnnotation(PrecursorIonType.class);
        final FragmentAnnotation<Ionization> ia = tree.getOrCreateFragmentAnnotation(Ionization.class);
        new PostOrderTraversal<Fragment>(tree.getCursor(f)).run(new PostOrderTraversal.Run<Fragment>() {
            @Override
            public void run(Fragment vertex, boolean isRoot) {
                PrecursorIonType precursorIonType = PrecursorIonType.getPrecursorIonType(vertex.getIonization());
                precursorIonType = precursorIonType.substituteAdduct(ionType.getAdduct()).substituteInsource(ionType.getInSourceFragmentation());
                fa.set(vertex, precursorIonType);
                ia.set(vertex, vertex.ionization);
            }
        });
    }


    private void setIonizationAsPrecursorIonTypeToEachNode(FTree tree, Fragment f) {
        final FragmentAnnotation<PrecursorIonType> fa = tree.getOrCreateFragmentAnnotation(PrecursorIonType.class);
        final FragmentAnnotation<Ionization> ia = tree.getOrCreateFragmentAnnotation(Ionization.class);
        new PostOrderTraversal<Fragment>(tree.getCursor(f)).run(new PostOrderTraversal.Run<Fragment>() {
            @Override
            public void run(Fragment vertex, boolean isRoot) {
                final PrecursorIonType precursorIonType = PrecursorIonType.getPrecursorIonType(vertex.getIonization());
                fa.set(vertex, precursorIonType);
                ia.set(vertex, vertex.getIonization());
            }
        });
    }
}
