import world.World;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by arthu on 21/02/2017.
 */
public class KeyHandler implements KeyListener {

    World world = null;

    /**
     * Keyhandler that updates the given world
     * @param world
     */
    public KeyHandler(World world){
        this.world=world;

        JFrame aWindow = new JFrame("Key Handler");

        aWindow.setBounds(50, 100, 300, 300);
        aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aWindow.addKeyListener(this);
        aWindow.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        world.move(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        world.setKey(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        world.removeKey(e);
    }
}
