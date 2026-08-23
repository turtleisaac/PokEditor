package io.github.turtleisaac.pokeditor.gui_old;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Property-based tests for {@link JCheckboxTree}, a tri-state checkbox tree.
 *
 * <p>THEORY. The check state of a tree is a function from nodes to {UNCHECKED, PARTIAL, CHECKED}
 * that is completely determined by which leaves are checked:
 * <ul>
 *   <li><b>Downward propagation.</b> Checking a node checks its entire subtree; unchecking it
 *       unchecks the entire subtree. The operation is a subtree-wide constant function.</li>
 *   <li><b>Upward consistency.</b> A node is CHECKED iff all of its children are CHECKED,
 *       UNCHECKED iff none of its descendants are checked, and PARTIAL otherwise. In this
 *       implementation those two predicates are the fields {@code allChildrenSelected} (fully
 *       checked) and {@code isSelected} (at least one descendant checked).</li>
 *   <li><b>Leaves are two-state.</b> A node with no children has no partial state to be in.</li>
 *   <li><b>Locality.</b> An operation inside one subtree changes nothing in a sibling subtree.</li>
 *   <li><b>Idempotence.</b> Repeating an operation changes nothing anywhere.</li>
 * </ul>
 * These are invariants, not endpoints: {@link #assertInvariant} re-derives them over the whole
 * tree and is called after <em>every</em> mutation below.
 */
public class JCheckboxTreeTest
{
    private JCheckboxTree tree;
    private final Map<String, DefaultMutableTreeNode> nodes = new LinkedHashMap<>();

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void buildTree()
    {
        nodes.clear();
        DefaultMutableTreeNode root = node("root");
        DefaultMutableTreeNode a = node("A");
        DefaultMutableTreeNode b = node("B");
        DefaultMutableTreeNode c = node("C");           // leaf child of root
        DefaultMutableTreeNode a1 = node("A1");
        DefaultMutableTreeNode a2 = node("A2");
        DefaultMutableTreeNode b1 = node("B1");
        DefaultMutableTreeNode b2 = node("B2");         // leaf
        DefaultMutableTreeNode b1a = node("B1a");
        DefaultMutableTreeNode b1b = node("B1b");

        root.add(a);
        root.add(b);
        root.add(c);
        a.add(a1);
        a.add(a2);
        b.add(b1);
        b.add(b2);
        b1.add(b1a);
        b1.add(b1b);

        tree = new JCheckboxTree(root);
    }

    private DefaultMutableTreeNode node(String name)
    {
        DefaultMutableTreeNode created = new DefaultMutableTreeNode(name);
        nodes.put(name, created);
        return created;
    }

    private TreePath path(String name)
    {
        return new TreePath(nodes.get(name).getPath());
    }

    private JCheckboxTree.CheckedNode state(TreePath path)
    {
        return tree.nodesCheckingState.get(path);
    }

    /** Exactly what the widget's mouse handler does for a click on the given node. */
    private void click(String name)
    {
        TreePath target = path(name);
        boolean checkMode = !state(target).isSelected();
        tree.checkSubTree(target, checkMode);
        tree.updatePredecessorsWithCheckMode(target, checkMode);
    }

    private List<DefaultMutableTreeNode> allNodes()
    {
        List<DefaultMutableTreeNode> all = new ArrayList<>();
        collect((DefaultMutableTreeNode) tree.getModel().getRoot(), all);
        return all;
    }

    private void collect(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> into)
    {
        into.add(node);
        for (int i = 0; i < node.getChildCount(); i++)
            collect((DefaultMutableTreeNode) node.getChildAt(i), into);
    }

    /** Re-derives every tri-state invariant across the whole tree. */
    private void assertInvariant(String after)
    {
        for (DefaultMutableTreeNode node : allNodes())
        {
            TreePath nodePath = new TreePath(node.getPath());
            JCheckboxTree.CheckedNode cn = state(nodePath);

            // Domain completeness: the check-state map is a total function on the tree's nodes.
            assertThat(cn).as("%s: no check state tracked for %s", after, node).isNotNull();

            // The cached structural flag must mirror the model it is derived from.
            assertThat(cn.isHasChildren())
                    .as("%s: hasChildren for %s", after, node)
                    .isEqualTo(node.getChildCount() > 0);

            if (node.getChildCount() > 0)
            {
                boolean anyChildSelected = false;
                boolean allChildrenFull = true;
                for (int i = 0; i < node.getChildCount(); i++)
                {
                    JCheckboxTree.CheckedNode child = state(nodePath.pathByAddingChild(node.getChildAt(i)));
                    anyChildSelected |= child.isSelected();
                    allChildrenFull &= child.isAllChildrenSelected();
                }
                // Upward consistency, both halves.
                assertThat(cn.isSelected())
                        .as("%s: %s must be marked iff some child is marked", after, node)
                        .isEqualTo(anyChildSelected);
                assertThat(cn.isAllChildrenSelected())
                        .as("%s: %s must be fully checked iff every child is fully checked", after, node)
                        .isEqualTo(allChildrenFull);
                // Fully checked implies checked: a parent cannot be "all children checked" while
                // claiming nothing beneath it is checked.
                if (cn.isAllChildrenSelected())
                    assertThat(cn.isSelected()).as("%s: %s full but unmarked", after, node).isTrue();
            }
            else
            {
                // A leaf has no children to disagree about, so it is never greyed.
                assertThat(tree.isSelectedPartially(nodePath))
                        .as("%s: leaf %s must never be partially checked", after, node)
                        .isFalse();
            }

            // The partial predicate is exactly "checked but not fully checked".
            assertThat(tree.isSelectedPartially(nodePath))
                    .as("%s: partial predicate for %s", after, node)
                    .isEqualTo(cn.isSelected() && cn.isHasChildren() && !cn.isAllChildrenSelected());

            // The reported set of checked paths must agree with the per-node state; a path that
            // renders as checked but is missing from getCheckedPaths() is silently dropped work.
            assertThat(tree.checkedPaths.contains(nodePath))
                    .as("%s: getCheckedPaths() membership for %s", after, node)
                    .isEqualTo(cn.isSelected());
        }
    }

    private Map<String, String> snapshot()
    {
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (DefaultMutableTreeNode node : allNodes())
        {
            JCheckboxTree.CheckedNode cn = state(new TreePath(node.getPath()));
            snapshot.put(node.getUserObject().toString(), cn.isSelected() + "/" + cn.isAllChildrenSelected());
        }
        return snapshot;
    }

    @Test
    @DisplayName("a freshly built tree has nothing checked and satisfies the invariant")
    void freshTreeIsConsistent()
    {
        // The initial state is the everywhere-unchecked function, which trivially satisfies both
        // halves of upward consistency.
        assertInvariant("construction");
        assertThat(tree.getCheckedPaths()).isEmpty();
        for (DefaultMutableTreeNode node : allNodes())
            assertThat(state(new TreePath(node.getPath())).isSelected()).as("%s", node).isFalse();
    }

    @Test
    @DisplayName("checking a node checks its whole subtree and nothing outside it")
    void checkingPropagatesDownAndStaysLocal()
    {
        Map<String, String> before = snapshot();
        click("B");
        assertInvariant("click B");

        // Downward propagation: the subtree rooted at B is now a constant CHECKED.
        for (String name : new String[] {"B", "B1", "B2", "B1a", "B1b"})
        {
            assertThat(state(path(name)).isSelected()).as("%s selected", name).isTrue();
            assertThat(state(path(name)).isAllChildrenSelected()).as("%s fully checked", name).isTrue();
        }

        // Locality: the sibling subtree A and the leaf C are untouched by an operation on B.
        for (String name : new String[] {"A", "A1", "A2", "C"})
            assertThat(snapshot().get(name)).as("%s must be unchanged", name).isEqualTo(before.get(name));
    }

    @Test
    @DisplayName("checking one deep leaf makes exactly its ancestors partial")
    void checkingALeafMakesAncestorsPartial()
    {
        click("B1a");
        assertInvariant("click B1a");

        // Upward consistency: one checked leaf marks each ancestor as checked-but-not-full.
        for (String name : new String[] {"B1", "B", "root"})
        {
            assertThat(tree.isSelectedPartially(path(name))).as("%s partial", name).isTrue();
            assertThat(state(path(name)).isAllChildrenSelected()).as("%s not full", name).isFalse();
        }
        // ...and leaves nothing else marked.
        for (String name : new String[] {"A", "A1", "A2", "B2", "B1b", "C"})
            assertThat(state(path(name)).isSelected()).as("%s untouched", name).isFalse();
    }

    @Test
    @DisplayName("checking every child promotes the parent from partial to fully checked")
    void allChildrenCheckedPromotesTheParent()
    {
        click("B1a");
        assertInvariant("click B1a");
        assertThat(tree.isSelectedPartially(path("B1"))).isTrue();

        click("B1b");
        assertInvariant("click B1b");
        // "Checked iff all children checked" is now satisfied for B1, so it must stop being grey.
        assertThat(state(path("B1")).isAllChildrenSelected()).isTrue();
        assertThat(tree.isSelectedPartially(path("B1"))).isFalse();
        // B still has an unchecked child (B2), so B stays partial: the rule is per-node.
        assertThat(tree.isSelectedPartially(path("B"))).isTrue();
    }

    @Test
    @DisplayName("checking an already-checked node changes nothing anywhere")
    void checkingIsIdempotent()
    {
        click("B1a");
        assertInvariant("click B1a");
        Map<String, String> after = snapshot();

        TreePath target = path("B1a");
        tree.checkSubTree(target, true);
        tree.updatePredecessorsWithCheckMode(target, true);
        assertInvariant("re-check B1a");

        // f(f(x)) == f(x): a second identical operation is the identity on the whole tree.
        assertThat(snapshot()).isEqualTo(after);
    }

    @Test
    @DisplayName("unchecking undoes checking exactly (involution on the whole tree)")
    void checkThenUncheckRestoresTheInitialState()
    {
        Map<String, String> initial = snapshot();
        click("B");
        assertInvariant("click B");
        click("B");
        assertInvariant("click B again");
        // Toggling a node twice is the identity, including for every ancestor it dragged along.
        assertThat(snapshot()).isEqualTo(initial);
        assertThat(tree.getCheckedPaths()).isEmpty();
    }

    @Test
    @DisplayName("checkRoot() checks every node in the tree")
    void checkRootChecksEverything()
    {
        tree.checkRoot();
        assertInvariant("checkRoot");

        // The root's subtree is the whole tree, so downward propagation from it is total.
        List<DefaultMutableTreeNode> all = allNodes();
        assertThat(tree.getCheckedPaths()).hasSize(all.size());
        for (DefaultMutableTreeNode node : all)
        {
            assertThat(state(new TreePath(node.getPath())).isSelected()).as("%s", node).isTrue();
            assertThat(state(new TreePath(node.getPath())).isAllChildrenSelected()).as("%s", node).isTrue();
            assertThat(tree.isSelectedPartially(new TreePath(node.getPath()))).as("%s", node).isFalse();
        }
    }

    @Test
    @DisplayName("the invariant survives a long random sequence of user gestures")
    void invariantHoldsAfterEveryGestureInARandomSequence()
    {
        List<String> names = new ArrayList<>(nodes.keySet());
        Random random = new Random(20260823L);
        StringBuilder history = new StringBuilder();

        for (int step = 0; step < 120; step++)
        {
            String target = names.get(random.nextInt(names.size()));
            history.append(target).append(' ');
            click(target);
            // The invariant is a state predicate, so it must hold after every single operation,
            // not merely once the sequence has finished.
            assertInvariant("gesture sequence [" + history + "]");
        }
    }

    @Test
    @DisplayName("the public checkSubTree leaves the tree in a consistent state")
    void publicCheckSubTreeMaintainsTheInvariant()
    {
        // checkSubTree is public API. Any public mutator must leave its object satisfying the
        // object's own invariant - a caller that is not also told to run the private upward pass
        // otherwise ends up with a parent that renders as unchecked while its children are checked,
        // and getCheckedPaths() that omits them.
        tree.checkSubTree(path("B1a"), true);
        assertInvariant("public checkSubTree(B1a, true)");
    }

    @Test
    @DisplayName("swapping the model resets the check state and keeps the invariant")
    void modelSwapResetsCheckingState()
    {
        click("B");
        assertInvariant("click B");

        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("newRoot");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode("newChild");
        newRoot.add(child);
        tree.setModel(new DefaultTreeModel(newRoot));

        // The check state is a function on the CURRENT model's nodes; after a swap its domain is
        // the new tree and nothing in it can be checked, since the user has checked nothing there.
        assertInvariant("model swap");
        assertThat(tree.getCheckedPaths()).isEmpty();
        assertThat(state(new TreePath(newRoot.getPath()))).isNotNull();
        assertThat(state(new TreePath(child.getPath()))).isNotNull();

        assertThatCode(tree::checkRoot).doesNotThrowAnyException();
        assertInvariant("checkRoot after model swap");
        assertThat(tree.getCheckedPaths()).hasSize(2);
    }

    @Test
    @DisplayName("a null model is accepted, as JTree specifies")
    void nullModelIsAccepted()
    {
        // JTree.setModel accepts null (getModel() may return null and the UI copes), so an
        // override that dies on it narrows the contract of the class it extends.
        assertThatCode(() -> tree.setModel(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a node added to the model is tracked by the check state")
    void nodesAddedToTheModelAreTracked()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode added = new DefaultMutableTreeNode("A3");
        model.insertNodeInto(added, nodes.get("A"), nodes.get("A").getChildCount());

        // The check-state map must remain a total function on the model's nodes: the model fires
        // a structural change, so a widget deriving state from it has to follow. An untracked node
        // makes every later operation on its ancestors dereference a missing entry.
        assertThat(state(new TreePath(added.getPath()))).as("check state for a newly inserted node").isNotNull();
        assertThatCode(tree::checkRoot).doesNotThrowAnyException();
    }
}
