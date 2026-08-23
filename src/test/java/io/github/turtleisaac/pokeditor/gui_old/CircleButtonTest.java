package io.github.turtleisaac.pokeditor.gui_old;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Property-based tests for {@link CircleButton}.
 *
 * <p>THEORY. A round button's hit region is the disc inscribed in its bounds: centre at the
 * component's centre, radius half of the smaller side (the largest circle that fits). The
 * geometric consequences are exact and independent of the implementation:
 * <ul>
 *   <li>the centre is inside;</li>
 *   <li>a point at distance strictly less than the radius is inside, one at distance strictly
 *       greater is outside;</li>
 *   <li>the four corners of the bounding box are at distance sqrt((w/2)^2 + (h/2)^2) >= r, and are
 *       therefore outside. This is the discriminating case: the inherited rectangular
 *       {@code Component.contains} accepts every point of the bounds, corners included, so a
 *       button that never overrode it would look round but click square.</li>
 * </ul>
 * The radius is re-derived from the component's size at each size tested, so nothing here is
 * hard-coded to one geometry.
 */
public class CircleButtonTest
{
    private static final int[][] SIZES = {{40, 40}, {60, 40}, {40, 60}, {100, 100}, {24, 90}, {31, 31}, {17, 45}};

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private static CircleButton buttonOfSize(int width, int height)
    {
        CircleButton button = new CircleButton();
        button.setText("Go");
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        button.setSize(width, height);
        return button;
    }

    /** Radius of the inscribed circle, in pixels, derived from the component size alone. */
    private static double radiusOf(CircleButton button)
    {
        return Math.min(button.getWidth(), button.getHeight()) / 2.0;
    }

    private static double distanceToCentre(CircleButton button, int x, int y)
    {
        return Point2D.distance(x, y, button.getWidth() / 2.0, button.getHeight() / 2.0);
    }

    @Test
    @DisplayName("the hit region is the inscribed disc, point by point over the whole bounds")
    void hitRegionIsTheInscribedDisc()
    {
        // Even sizes only: the centre and the radius then land exactly on the pixel lattice, so
        // the geometric prediction is unambiguous. Points within one pixel of the rim are skipped
        // because whether the rim itself counts as inside is a convention, not a geometric fact.
        for (int[] size : new int[][] {{40, 40}, {60, 40}, {40, 60}, {100, 100}})
        {
            CircleButton button = buttonOfSize(size[0], size[1]);
            double radius = radiusOf(button);

            for (int x = 0; x < button.getWidth(); x++)
            {
                for (int y = 0; y < button.getHeight(); y++)
                {
                    double distance = distanceToCentre(button, x, y);
                    if (distance <= radius - 1.0)
                        assertThat(button.contains(x, y))
                                .as("%dx%d: (%d,%d) is %.2f from the centre, inside radius %.1f",
                                        size[0], size[1], x, y, distance, radius)
                                .isTrue();
                    else if (distance >= radius + 1.0)
                        assertThat(button.contains(x, y))
                                .as("%dx%d: (%d,%d) is %.2f from the centre, outside radius %.1f",
                                        size[0], size[1], x, y, distance, radius)
                                .isFalse();
                }
            }
        }
    }

    @Test
    @DisplayName("the corners of the bounding box are outside the circle, at every size")
    void cornersAreOutside()
    {
        for (int[] size : SIZES)
        {
            CircleButton button = buttonOfSize(size[0], size[1]);
            int w = button.getWidth();
            int h = button.getHeight();
            int[][] corners = {{0, 0}, {w - 1, 0}, {0, h - 1}, {w - 1, h - 1}};

            for (int[] corner : corners)
            {
                // A corner is at distance sqrt((w/2)^2+(h/2)^2) from the centre, which is at least
                // the inscribed radius min(w,h)/2 and strictly greater whenever w,h > 0. Accepting
                // it means the component is not round at all.
                assertThat(button.contains(corner[0], corner[1]))
                        .as("%dx%d: corner (%d,%d) must not be inside the circle", w, h, corner[0], corner[1])
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("the centre is inside and remains inside across resizes")
    void centreIsAlwaysInside()
    {
        for (int[] size : SIZES)
        {
            CircleButton button = buttonOfSize(size[0], size[1]);
            // Distance 0 < r for any circle with a positive radius.
            assertThat(button.contains(button.getWidth() / 2, button.getHeight() / 2))
                    .as("%dx%d: the centre must be inside", size[0], size[1])
                    .isTrue();
        }
    }

    @Test
    @DisplayName("points just inside the rim hit and points just outside miss, at every size")
    void rimBehaviourFollowsTheRadius()
    {
        for (int[] size : SIZES)
        {
            CircleButton button = buttonOfSize(size[0], size[1]);
            int cx = button.getWidth() / 2;
            int cy = button.getHeight() / 2;
            int radius = (int) radiusOf(button);

            // Straight out along the four axes: one pixel short of the rim is inside the disc,
            // one pixel past it is outside. Re-derived from the size, so this also fixes the
            // resize-invariance of the hit test.
            assertThat(button.contains(cx + radius - 1, cy)).as("%dx%d: inside right", size[0], size[1]).isTrue();
            assertThat(button.contains(cx - radius + 1, cy)).as("%dx%d: inside left", size[0], size[1]).isTrue();
            assertThat(button.contains(cx, cy + radius - 1)).as("%dx%d: inside below", size[0], size[1]).isTrue();
            assertThat(button.contains(cx, cy - radius + 1)).as("%dx%d: inside above", size[0], size[1]).isTrue();

            assertThat(button.contains(cx + radius + 1, cy)).as("%dx%d: outside right", size[0], size[1]).isFalse();
            assertThat(button.contains(cx - radius - 1, cy)).as("%dx%d: outside left", size[0], size[1]).isFalse();
            assertThat(button.contains(cx, cy + radius + 1)).as("%dx%d: outside below", size[0], size[1]).isFalse();
            assertThat(button.contains(cx, cy - radius - 1)).as("%dx%d: outside above", size[0], size[1]).isFalse();
        }
    }

    @Test
    @DisplayName("points outside the bounds are outside the button")
    void pointsBeyondTheBoundsAreOutside()
    {
        CircleButton button = buttonOfSize(50, 50);
        // The disc is contained in the bounds, so anything outside the bounds is outside the disc.
        assertThat(button.contains(-5, 25)).isFalse();
        assertThat(button.contains(25, -5)).isFalse();
        assertThat(button.contains(500, 25)).isFalse();
        assertThat(button.contains(25, 500)).isFalse();
    }

    @Test
    @DisplayName("the preferred size is computable without a native peer")
    void preferredSizeIsComputableBeforeDisplay()
    {
        CircleButton button = new CircleButton();
        button.setText("Go");
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        // Every layout manager queries the preferred size while the component is still
        // undisplayed, when getGraphics() is specified to return null. A preferred size that can
        // only be computed once a peer exists cannot be laid out at all.
        assertThatCode(button::getPreferredSize).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an explicitly set preferred size wins, as JComponent specifies")
    void explicitPreferredSizeIsHonoured()
    {
        CircleButton button = new CircleButton();
        button.setPreferredSize(new Dimension(64, 64));

        // JComponent.getPreferredSize: if a preferred size has been set to a non-null value, it is
        // returned. An override that ignores it takes away the caller's only way to size the
        // button.
        assertThat(button.getPreferredSize()).isEqualTo(new Dimension(64, 64));
    }

    @Test
    @DisplayName("the computed preferred size is square and large enough for the label")
    void computedPreferredSizeIsSquareAndFitsTheLabel()
    {
        F_MeasurableCircleButton button = new F_MeasurableCircleButton();
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        for (String text : new String[] {"", "Go", "A much longer caption"})
        {
            button.setText(text);
            Dimension preferred = button.getPreferredSize();
            FontMetrics metrics = button.getGraphics().getFontMetrics(button.getFont());

            // A circle's bounding box is square; anything else would leave the drawn circle
            // smaller than the space reserved for it.
            assertThat(preferred.width).as("square for text <%s>", text).isEqualTo(preferred.height);
            // The label is drawn inside the circle, so the diameter must at least span the label's
            // own width and height.
            assertThat(preferred.width).as("fits the label width of <%s>", text).isGreaterThanOrEqualTo(metrics.stringWidth(text));
            assertThat(preferred.height).as("fits the label height of <%s>", text).isGreaterThanOrEqualTo(metrics.getHeight());
        }
    }

    /**
     * Test double: supplies the off-screen {@link Graphics} that an undisplayed component does not
     * have, so the preferred-size computation itself can be exercised in a headless JVM.
     */
    private static class F_MeasurableCircleButton extends CircleButton
    {
        private final BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);

        @Override
        public Graphics getGraphics()
        {
            return image.getGraphics();
        }
    }
}
