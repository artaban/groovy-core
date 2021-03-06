import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode

/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Specification tests for the traits feature
 */
class TraitsSpecificationTest extends GroovyTestCase {
    void testTraitDeclaration() {
        assertScript '''// tag::flying_simple[]
trait FlyingAbility {                           // <1>
        String fly() { "I'm flying!" }          // <2>
}
// end::flying_simple[]

// tag::bird[]
class Bird implements FlyingAbility {}          // <1>
def b = new Bird()                              // <2>
assert b.fly() == "I'm flying!"                 // <3>
// end::bird[]

'''
    }

    void testAbstractMethodInTrait() {
        assertScript '''// tag::greetable[]
trait Greetable {
    abstract String name()                              // <1>
    String greeting() { "Hello, ${name()}!" }           // <2>
}
// end::greetable[]

// tag::greetable_person[]
class Person implements Greetable {                     // <1>
    String name() { 'Bob' }                             // <2>
}

def p = new Person()
assert p.greeting() == 'Hello, Bob!'                    // <3>
// end::greetable_person[]
'''
    }

    void testTraitImplementingInterface() {
        assertScript '''// tag::trait_implementing_interface[]
interface Named {                                       // <1>
    String name()
}
trait Greetable implements Named {                      // <2>
    String greeting() { "Hello, ${name()}!" }
}
class Person implements Greetable {                     // <3>
    String name() { 'Bob' }                             // <4>
}

def p = new Person()
assert p.greeting() == 'Hello, Bob!'                    // <5>
assert p instanceof Named                               // <6>
assert p instanceof Greetable                           // <7>
// end::trait_implementing_interface[]'''
    }

    void testTraitWithProperty() {
        assertScript '''// tag::trait_with_property[]
trait Named {
    String name                             // <1>
}
class Person implements Named {}            // <2>
def p = new Person(name: 'Bob')             // <3>
assert p.name == 'Bob'                      // <4>
assert p.getName() == 'Bob'                 // <5>
// end::trait_with_property[]'''
    }

    void testCompositionOfTraits() {
        assertScript '''trait FlyingAbility {
    String fly() { "I'm flying!" }
}

// tag::speaking_simple[]
trait SpeakingAbility {
    String speak() { "I'm speaking!" }
}
// end::speaking_simple[]

// tag::speakingduck[]
class Duck implements FlyingAbility, SpeakingAbility {} // <1>

def d = new Duck()                                      // <2>
assert d.fly() == "I'm flying!"                         // <3>
assert d.speak() == "I'm speaking!"                     // <4>
// end::speakingduck[]
'''
    }

    void testOverridableMethod() {
        assertScript '''trait FlyingAbility {
    String fly() { "I'm flying!" }
}

trait SpeakingAbility {
    String speak() { "I'm speaking!" }
}

// tag::quackingduck[]
class Duck implements FlyingAbility, SpeakingAbility {
    String quack() { "Quack!" }                         // <1>
    String speak() { quack() }                          // <2>
}

def d = new Duck()
assert d.fly() == "I'm flying!"                         // <3>
assert d.quack() == "Quack!"                            // <4>
assert d.speak() == "Quack!"                            // <5>
// end::quackingduck[]'''
    }

    void testPrivateMethodInTrait() {
        assertScript '''// tag::private_method_in_trait[]
trait Greeter {
    private String greetingMessage() {                      // <1>
        'Hello from a private method!'
    }
    String greet() {
        def m = greetingMessage()                           // <2>
        println m
        m
    }
}
class GreetingMachine implements Greeter {}                 // <3>
def g = new GreetingMachine()
assert g.greet() == "Hello from a private method!"          // <4>
try {
    assert g.greetingMessage()                              // <5>
} catch (MissingMethodException e) {
    println "greetingMessage is private in trait"
}
// end::private_method_in_trait[]
'''
    }

    void testTraitWithPrivateField() {
        assertScript '''// tag::trait_with_private_field[]
trait Counter {
    private int count = 0                   // <1>
    int count() { count += 1; count }       // <2>
}
class Foo implements Counter {}             // <3>
def f = new Foo()
assert f.count() == 1                       // <4>
// end::trait_with_private_field[]
'''
    }

    void testTraitWithPublicField() {
        assertScript '''// tag::trait_with_public_field[]
trait Named {
    public String name                      // <1>
}
class Person implements Named {}            // <2>
def p = new Person()                        // <3>
p.Named__name = 'Bob'                       // <4>
// end::trait_with_public_field[]'''
    }

    void testRemappedName() {
        def clazz = new ClassNode("my.package.Foo", 0, ClassHelper.OBJECT_TYPE)
        assert org.codehaus.groovy.transform.trait.Traits.remappedFieldName(clazz, "bar") == 'my_package_Foo__bar'
    }

    void testDuckTyping() {
        assertScript '''// tag::ducktyping[]
trait SpeakingDuck {
    String speak() { quack() }                      // <1>
}
class Duck implements SpeakingDuck {
    String methodMissing(String name, args) {
        "${name.capitalize()}!"                     // <2>
    }
}
def d = new Duck()
assert d.speak() == 'Quack!'                        // <3>
// end::ducktyping[]'''
    }

    void testTraitInheritance() {
        assertScript '''// tag::trait_inherit[]
trait Named {
    String name                                     // <1>
}
trait Polite extends Named {                        // <2>
    String introduce() { "Hello, I am $name" }      // <3>
}
class Person implements Polite {}
def p = new Person(name: 'Alice')                   // <4>
assert p.introduce() == 'Hello, I am Alice'         // <5>
// end::trait_inherit[]
'''
    }

    void testMethodMissingInTrait() {
        assertScript '''// tag::dynamicobject[]
trait DynamicObject {                               // <1>
    private Map props = [:]
    def methodMissing(String name, args) {
        name.toUpperCase()
    }
    def propertyMissing(String prop) {
        props['prop']
    }
    void setProperty(String prop, Object value) {
        props['prop'] = value
    }
}

class Dynamic implements DynamicObject {
    String existingProperty = 'ok'                  // <2>
    String existingMethod() { 'ok' }                // <3>
}
def d = new Dynamic()
assert d.existingProperty == 'ok'                   // <4>
assert d.foo == null                                // <5>
d.foo = 'bar'                                       // <6>
assert d.foo == 'bar'                               // <7>
assert d.existingMethod() == 'ok'                   // <8>
assert d.someMethod() == 'SOMEMETHOD'               // <9>
// end::dynamicobject[]'''
    }

    void testDefaultMultipleInheritanceResolution() {
        assertScript '''// tag::multiple_inherit_default[]
trait A {
    String exec() { 'A' }               // <1>
}
trait B {
    String exec() { 'B' }               // <2>
}
class C implements A,B {}               // <3>
// end::multiple_inherit_default[]

// tag::multiple_inherit_default_assert[]
def c = new C()
assert c.exec() == 'B'
// end::multiple_inherit_default_assert[]'''
    }

    void testUserMultipleInheritanceResolution() {
        assertScript '''trait A {
    String exec() { 'A' }
}
trait B {
    String exec() { 'B' }
}
// tag::multiple_inherit_user[]
class C implements A,B {
    String exec() { A.super.exec() }    // <1>
}
def c = new C()
assert c.exec() == 'A'                  // <2>
// end::multiple_inherit_user[]'''
    }

    void testRuntimeCoercion() {
        assertScript '''
// tag::runtime_header[]
trait Extra {
    String extra() { "I'm an extra method" }            // <1>
}
class Something {                                       // <2>
    String doSomething() { 'Something' }                // <3>
}
// end::runtime_header[]

try {
// tag::runtime_fail[]
def s = new Something()
s.extra()
// end::runtime_fail[]
} catch (MissingMethodException e) {}

// tag::runtime_success[]
def s = new Something() as Extra                        // <1>
s.extra()                                               // <2>
s.doSomething()                                         // <3>
// end::runtime_success[]'''
    }

    void testWithTraits() {
        assertScript '''// tag::withtraits_header[]
trait A { void methodFromA() {} }
trait B { void methodFromB() {} }

class C {}

def c = new C()
// end::withtraits_header[]
try {
// tag::withtraits_fail[]
c.methodFromA()                     // <1>
c.methodFromB()                     // <2>
// end::withtraits_fail[]
} catch (MissingMethodException e) {}
// tag::withtraits_success[]
def d = c.withTraits A, B           // <3>
d.methodFromA()                     // <4>
d.methodFromB()                     // <5>
// end::withtraits_success[]
'''
    }

    void testSAMCoercion() {
        assertScript '''import org.codehaus.groovy.runtime.Greeter

// tag::sam_trait[]
trait Greeter {
    String greet() { "Hello $name" }        // <1>
    abstract String getName()               // <2>
}
// end::sam_trait[]

// tag::sam_trait_assignment[]
Greeter greeter = { 'Alice' }               // <1>
// end::sam_trait_assignment[]

// tag::sam_trait_method[]
void greet(Greeter g) { println g.greet() } // <1>
greet { 'Alice' }                           // <2>
// end::sam_trait_method[]'''
    }

    void testForceOverride() {
        assertScript '''
// tag::forceoverride_header[]
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer

class SomeTest extends GroovyTestCase {
    def config
    def shell

    void setup() {
        config = new CompilerConfiguration()
        shell = new GroovyShell(config)
    }
    void testSomething() {
        assert shell.evaluate('1+1') == 2
    }
    void otherTest() { /* ... */ }
}
// end::forceoverride_header[]

class SomeTest2 extends SomeTest {}

/*
// tag::forceoverride_extends[]
class AnotherTest extends SomeTest {
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( ... )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_extends[]

// tag::forceoverride_extends2[]
class YetAnotherTest extends SomeTest {
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( ... )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_extends2[]
*/

// tag::forceoverride_trait[]
trait MyTestSupport {
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( new ASTTransformationCustomizer(CompileStatic) )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_trait[]

// tag::forceoverride_miss_header[]
class AnotherTest extends SomeTest implements MyTestSupport {}
class YetAnotherTest extends SomeTest2 implements MyTestSupport {}
// end::forceoverride_miss_header[]

def t = new AnotherTest()
t.setup()
assert t.config.compilationCustomizers.empty
'''
    }

    void testForceOverrideFixed() {
        assertScript '''
import groovy.transform.CompileStatic
import groovy.transform.ForceOverride
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer

class SomeTest extends GroovyTestCase {
    def config
    def shell

    void setup() {
        config = new CompilerConfiguration()
        shell = new GroovyShell(config)
    }
    void testSomething() {
        assert shell.evaluate('1+1') == 2
    }
    void otherTest() { /* ... */ }
}
// tag::forceoverride_trait_fixed[]
trait MyTestSupport {
    @ForceOverride
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( new ASTTransformationCustomizer(CompileStatic) )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_trait_fixed[]

class AnotherTest extends SomeTest implements MyTestSupport {}

def t = new AnotherTest()
t.setup()
assert !t.config.compilationCustomizers.empty
'''
    }

    void testRuntimeForceOverride() {
        assertScript '''import groovy.transform.ForceOverride
// tag::runtime_forceoverride[]
class Person {
    String name                                         // <1>
}
trait BobNoOverride {
    String getName() { 'Bob' }                          // <2>
}
trait BobForceOverride {
    @ForceOverride String getName() { 'Bob' }           // <3>
}

def p = new Person(name: 'Alice')
assert p.name == 'Alice'                                // <4>
def p1 = p as BobNoOverride                             // <5>
assert p1.name == 'Alice'                               // <6>
def p2 = p as BobForceOverride                          // <7>
assert p2.name == 'Bob'                                 // <8>
// end::runtime_forceoverride[]'''
    }

    void testDifferenceWithMixin() {
        assertScript '''// tag::diff_mixin[]
class A { String methodFromA() { 'A' } }        // <1>
class B { String methodFromB() { 'B' } }        // <2>
A.metaClass.mixin B                             // <3>
def o = new A()
assert o.methodFromA() == 'A'                   // <4>
assert o.methodFromB() == 'B'                   // <5>
assert o instanceof A                           // <6>
assert !(o instanceof B)                        // <7>
// end::diff_mixin[]'''
    }

    void testMeaningOfThis() {
        assertScript '''// tag::meaningofthis_header[]
trait Introspector {
    def whoAmI() { this }
}
class Foo implements Introspector {}
def foo = new Foo()
// end::meaningofthis_header[]
// tag::meaningofthis_snippet[]
foo.whoAmI()
// end::meaningofthis_snippet[]
// tag::meaningofthis_assert[]
assert foo.whoAmI().is(foo)
// end::meaningofthis_assert[]'''
    }

    void testPublicStaticFieldInTrait() {
        assertScript '''// tag::staticfield_header[]
trait TestHelper {
    public static boolean CALLED = false        // <1>
    static void init() {                        // <2>
        CALLED = true                           // <3>
    }
}
class Foo implements TestHelper {}
Foo.init()                                      // <4>
assert Foo.TestHelper__CALLED                   // <5>
// tag::staticfield_header[]

try {
// tag::staticfield_notontrait[]
    Foo.CALLED = true
// end::staticfield_notontrait[]
} catch (Exception e){}

// tag::staticfield_distinct[]
class Bar implements TestHelper {}              // <1>
class Baz implements TestHelper {}              // <2>
Bar.init()                                      // <3>
assert Bar.TestHelper__CALLED                   // <4>
assert !Baz.TestHelper__CALLED                  // <5>
// end::staticfield_distinct[]
'''
    }

    void testPrePostfixIsDisallowed() {
        def message = shouldFail '''
// tag::prefix_postfix[]
trait Counting {
    int x
    void inc() {
        x++                             // <1>
    }
    void dec() {
        --x                             // <2>
    }
}
class Counter implements Counting {}
def c = new Counter()
c.inc()
// end::prefix_postfix[]
        '''
        assert message.contains('Postfix expressions on trait fields/properties  are not supported')
        assert message.contains('Prefix expressions on trait fields/properties are not supported')
    }
}
