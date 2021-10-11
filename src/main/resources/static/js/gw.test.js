/**
 * Unit Test for Geoweaver
 * @author Jensen Sun
 * @date 10/9/2021
 */

GW.test = {

    run: function(){

        QUnit.module('process', GW.test.testProcess);

    },

    testProcess: function() {

        QUnit.test('create process dialog', function(assert) {
            // add menu item
            GW.process.addMenuItem({name: "test", id: "xyzxyz"}, "python"); 
            QUnit.assert.ok(true, "This function doesn't crash")
        });



    }

}

GW.test.run();