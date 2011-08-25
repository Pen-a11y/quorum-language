/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quorum.steps;

import org.quorum.execution.ExecutionStepVisitor;
import org.quorum.execution.ExpressionValue;
import org.quorum.symbols.Result;

/**
 * Boolean object to integer primitive.
 * 
 * @author Melissa Stefik
 */
public class BooleanReverseAutoBoxToIntegerStep extends IntegerReverseAutoBoxStep{
    @Override
    protected Result calculateOpcode(ExpressionValue value) {
        Result result = new Result();
        if(value.getResult().boolean_value)
            result.integer = 1;
        else
            result.integer = 0;

        result.type = Result.INTEGER;
        return result;
    }
    
    @Override
    public void visit(ExecutionStepVisitor visitor) {
        visitor.visit(this);
    }
}
