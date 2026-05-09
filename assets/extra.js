document$.subscribe(() => {
  mermaid.initialize({
    startOnLoad: true,
    theme: 'default',
    themeVariables: {
      fontSize: '18px',           // Increase text size
      lineColor: '#333333',
      textColor: '#000000'
    },
    flowchart: {
      useMaxWidth: false,         // Allow custom width
      htmlLabels: true
    }
  });

  mermaid.init(undefined, document.querySelectorAll(".language-mermaid"));
});

mermaid.initialize({
  theme: 'default',
  themeVariables: {
    fontSize: '18px',
    primaryColor: '#f9f',
    edgeLabelBackground: '#fff',
    nodeTextColor: '#000',
    clusterBorder: '#333'
  }
});
