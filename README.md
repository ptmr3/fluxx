# Fluxx

------

##### Fluxx is simple library created to empower Java and Kotlin developers in their journey for greatness. There are many different architectural styles that can be used, but none as efficient as Fluxx (** Obvious bias here... ). 



##### What is this Fluxx you speak of?

- Fluxx is a unidirectional, straight forward action/event based, reactive flow focused on maintainability, reliability, and performance. 
- Fluxx is a customized derivative of the Flux Architecture created by facebook (https://facebook.github.io/flux/). 
- Fluxx can be used in (m)any Java and Kotlin based applications from Web applications to Mobile.



##### Let's get more specific

![FluxxBasicFlow](https://github.com/ptmr3/fluxx/blob/master/FluxxBasicFlow.png)



The Fluxx flow consists of 3 major parts, and a couple optional extras depending on the complexity and dependencies in your project.

- **Initiator** - An Initiator is the one who initiates actions. This component is typically an entry point, a view, an endpoint, etc. Quite frequently an initiator will not only initiate actions, but will also subscribe to the outcome of those actions, referred to as reactions. 
- **ActionCreator** -  An ActionCreator is the creator of, and publisher of actions.  This component should only be called by initiators (never directly from a Store), however the ActionCreator can indeed be made a listener of external/miscellaneous events so that upon these events being received, an action can be published.
- **Store** -  A Store is the holder of all logic and state. This component should always be as specific as possible as to keep responsibility as segregated as possible. A Store should never access another store directly in any circumstance. Any public flags/variables should be read-only. In the event that an application is not able to be, or is not *yet* able to be split to the degree that only one Store is handling a certain piece of data or state, Accessors and/or Executors may be used.
  - ***Accessor*** - An Accessor can be used by Stores to read data/state information from another Store. 
  - ***Executor*** - An Executor can be used by Stores to hold and execute logic that is needed by multiple stores. 



**How do I start Fluxx-ing?**

to be continued...