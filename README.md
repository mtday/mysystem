# mysystem

An end-to-end Akka system used to build out a typical enterprise system with a lot of the common requirements. Just to
see how easy it is to use Akka as a core infrastructure component.

### Development Design Decisions

- Immutability is a core model object design tenant, and is used to solve many of the typical concurrency problems seen
  in distributed systems. In this project, the intent is for all model objects to be immutable, with embedded builder
  classes used to facilitate building and modifying the objects as appropriate.
- The only pom that contains dependency versions is the top-level project pom. All other poms do not specify versions
  on the dependencies.
- Actor messages are serialized using JSON, which supports flexibility during inter-system operations when the
  structures have been modifed across different versions. This prevents the typical java serialization problems when
  the model objects have been modified during system rolling upgrades.
  
