grammar proguard;

config
    : option* EOF
    ;

option
    : keepOptions
    | shrinkOptions
    | optimizationOptions
    | obfuscationOptions
    | preverificationOptions
    | generalOptions
    ;

// Keep Options
keepOptions
    : keep
    | keepclassmembers
    | keepclasseswithmembers
    | keepnames
    | keepclassmembernames
    | keepclasseswithmembernames
    | if
    ;

keep
    : '-keep' modifier* classSpecification
    ;

keepclassmembers
    : '-keepclassmembers' modifier* classSpecification
    ;

keepclasseswithmembers
    : '-keepclasseswithmembers' modifier* classSpecification
    ;

keepnames
    : '-keepnames' modifier* classSpecification
    ;

keepclassmembernames
    : '-keepclassmembernames' modifier* classSpecification
    ;

keepclasseswithmembernames
    : '-keepclasseswithmembernames' modifier* classSpecification
    ;

modifier
    : ',' 'includedescriptorclasses'
    | ',' 'includecode'
    | ',' 'allowshrinking'
    | ',' 'allowoptimization'
    | ',' 'allowobfuscation'
    ;

if
    : '-if' classSpecification keepOptions
    ;

// Shrink Options
shrinkOptions
    : dontshrink
    | whyareyoukeeping
    ;

dontshrink
    : '-dontshrink'
    ;

whyareyoukeeping
    : '-whyareyoukeeping' classSpecification
    ;

// Optimization Options
optimizationOptions
    : dontoptimize
    | optimizationpasses
    | assumenosideeffects
    | assumenoexternalsideeffects
    | assumenoescapingparameters
    | assumenoexternalreturnvalues
    | assumevalues
    | allowaccessmodification
    | mergeinterfacesaggressively
    | optimizeaggressively
    ;

dontoptimize
    : '-dontoptimize'
    ;

optimizationpasses
    : '-optimizationpasses' INT
    ;

assumenosideeffects
    : '-assumenosideeffects' classSpecification
    ;

assumenoexternalsideeffects
    : '-assumenoexternalsideeffects' classSpecification
    ;

assumenoescapingparameters
    : '-assumenoescapingparameters' classSpecification
    ;

assumenoexternalreturnvalues
    : '-assumenoexternalreturnvalues' classSpecification
    ;

assumevalues
    : '-assumevalues' classSpecification
    ;

allowaccessmodification
    : '-allowaccessmodification'
    ;

mergeinterfacesaggressively
    : '-mergeinterfacesaggressively'
    ;

optimizeaggressively
    : '-optimizeaggressively'
    ;

// Obfuscation Options
obfuscationOptions
    : dontobfuscate
    | overloadaggressively
    | useuniqueclassmembernames
    | dontusemixedcaseclassnames
    | keeppackagenames
    | flattenpackagehierarchy
    | keepattributes
    | keepparameternames
    | keepkotlinmetadata
    ;

dontobfuscate
    : '-dontobfuscate'
    ;

overloadaggressively
    : '-overloadaggressively'
    ;

useuniqueclassmembernames
    : '-useuniqueclassmembernames'
    ;

dontusemixedcaseclassnames
    : '-dontusemixedcaseclassnames'
    ;

keeppackagenames
    : '-keeppackagenames' filter?
    ;

flattenpackagehierarchy
    : '-flattenpackagehierarchy' ID
    ;

keepattributes
    : '-keepattributes' filter?
    ;

keepparameternames
    : '-keepparameternames'
    ;

keepkotlinmetadata
    : '-keepkotlinmetadata'
    ;

// Preverification Options
preverificationOptions
    : dontpreverify
    | microedition
    | android
    ;

dontpreverify
    : '-dontpreverify'
    ;

microedition
    : '-microedition'
    ;

android
    : '-android'
    ;

// General Options
generalOptions
    : verbose
    | dontnote
    | dontwarn
    | ignorewarnings
    | addconfigurationdebugging
    ;

verbose
    : '-verbose'
    ;

dontnote
    : '-dontnote' filter?
    ;

dontwarn
    : '-dontwarn' filter?
    ;

ignorewarnings
    : '-ignorewarnings'
    ;

addconfigurationdebugging
    : '-addconfigurationdebugging'
    ;

// Specification
classSpecification
    : annotation? typeModifier* classType className (extension)? memberBlock?
    ;

annotation
    : '@' className
    ;

typeModifier
    : '!'? ( 'public' | 'final' | 'abstract' | '@' )
    ;

classType
    : '!'? ( 'interface' | 'class' | 'enum' )
    ;

extension
    : ( 'extends' | 'implements' ) annotation? className
    ;

memberBlock
    : '{' memberSpecification* '}'
    ;

memberSpecification
    : fieldSpecification
    | methodSpecification
    | anyMemberSpecification
    ;

fieldSpecification
    : annotation? fieldModifier* ( '<fields>' | (type name ( '=' values )?) ) ';'
    ;

methodSpecification
    : annotation? methodModifier* ( '<methods>'
      | '<init>' arguments
      | name arguments
      | (type name arguments ( 'return' values )?)
      ) ';'
    ;

anyMemberSpecification
    : annotation? (fieldModifier | methodModifier)* '*' ';'
    ;

fieldModifier
    : '!'? ( 'public' | 'private' | 'protected' | 'static' | 'volatile' | 'transient' )
    ;

methodModifier
    : '!'? ( 'public' | 'private' | 'protected' | 'static' | 'synchronized' | 'native' | 'abstract' | 'strictfp' )
    ;

arguments : '(' (type ( ',' type )*)? ')' ;

className
    : classNameItem (',' classNameItem)*
    ;

classNameItem
    : '!'? name
    ;

// Filter
filter
    : filterItem (',' filterItem)*
    ;

filterItem
    : '!'? name
    ;

type      : name ;
values    : name ;

name      : namePart+ ;
namePart
    : ID
    | INT
    | '*'
    | '**'
    ;

INT: [0-9]+ ;
ID : [a-zA-Z_?$.<>[\]/\\:]+ ;
WS : [ \t\r\n]+ -> skip ;
COMMENT : '#' ~[\r\n]* -> skip;
