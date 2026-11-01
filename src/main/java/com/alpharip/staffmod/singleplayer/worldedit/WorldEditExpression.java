// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

/** Small arithmetic expression evaluator for bounded Alpha World Edit generation. */
public final class WorldEditExpression {
    private final String input;
    private int index;
    private double x,y,z;
    private WorldEditExpression(String input){this.input=input;}
    public static double evaluate(String expression,double x,double y,double z){
        WorldEditExpression parser=new WorldEditExpression(expression);
        parser.x=x;parser.y=y;parser.z=z;
        double value=parser.expression();
        parser.space();
        if(parser.index!=parser.input.length())throw new IllegalArgumentException("Unexpected expression token.");
        if(Double.isNaN(value)||Double.isInfinite(value))throw new IllegalArgumentException("Expression result is not finite.");
        return value;
    }
    private double expression(){
        double value=term();
        while(true){space();if(take('+'))value+=term();else if(take('-'))value-=term();else return value;}
    }
    private double term(){
        double value=factor();
        while(true){space();if(take('*'))value*=factor();else if(take('/')){double divisor=factor();if(divisor==0)throw new IllegalArgumentException("Division by zero.");value/=divisor;}else if(take('%')){double divisor=factor();if(divisor==0)throw new IllegalArgumentException("Division by zero.");value%=divisor;}else return value;}
    }
    private double factor(){
        space();
        if(take('+'))return factor();
        if(take('-'))return -factor();
        if(take('(')){double value=expression();space();if(!take(')'))throw new IllegalArgumentException("Missing closing parenthesis.");return value;}
        if(index<input.length()&&Character.isLetter(input.charAt(index))){
            char variable=Character.toLowerCase(input.charAt(index++));
            if(variable=='x')return x;if(variable=='y')return y;if(variable=='z')return z;
            throw new IllegalArgumentException("Only x, y, and z variables are supported.");
        }
        int start=index;
        while(index<input.length()&&(Character.isDigit(input.charAt(index))||input.charAt(index)=='.'))index++;
        if(start==index)throw new IllegalArgumentException("Expected a number, variable, or parenthesis.");
        try{return Double.parseDouble(input.substring(start,index));}
        catch(NumberFormatException ex){throw new IllegalArgumentException("Invalid number.");}
    }
    private void space(){while(index<input.length()&&Character.isWhitespace(input.charAt(index)))index++;}
    private boolean take(char value){if(index<input.length()&&input.charAt(index)==value){index++;return true;}return false;}
}
