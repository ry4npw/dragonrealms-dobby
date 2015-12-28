package pw.ry4n.dr.engine.model;

/**
 * A block represents a group of lines or commands that can be used in place of
 * a single command. A block begins with a left brace '{' and ends with a
 * right-brace '}'. A block can be used in expressions that typically only allow
 * for one command. For example, in the line {@code if_1 then <command>},
 * command can be replaced with multiple commands within one script block.
 * 
 * @author Ryan Powell
 */
public class GenieBlock extends GenieLine {

}
