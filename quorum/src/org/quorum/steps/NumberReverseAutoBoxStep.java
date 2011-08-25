/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quorum.steps;

import org.quorum.execution.DataEnvironment;
import org.quorum.execution.ExecutionStepVisitor;
import org.quorum.symbols.Result;
import org.quorum.execution.ExpressionValue;

/**
 * Auto-cast from the Number object to number
 *
 * @author Melissa Stefik
 */
public class NumberReverseAutoBoxStep extends NumberAutoBoxStep{


    @Override
    public void execute() {
        DataEnvironment data = vm.getDataEnvironment();
        ExpressionValue value = data.getRegister(getRegister());
        ExpressionValue result = new ExpressionValue();

        ExpressionValue variable = data.getObject(value.getObjectHash()).getVariable("value");

        result.setRegister(getResultRegister());
        Result res = calculateOpcode(value);
        res = variable.getResult();
        result.setType(res.getType());
        result.setResult(res);
        data.setRegister(getResultRegister(),result);
    }
    
    @Override
    public void visit(ExecutionStepVisitor visitor) {
        visitor.visit(this);
    }
}
