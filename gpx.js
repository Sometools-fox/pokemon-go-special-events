// Shared GPX 1.1 export for the activity list and detail page.
(function (root) {
  'use strict';
  const xml = value => String(value ?? '')
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\uFFFE\uFFFF]/g, '')
    .replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&apos;'}[c]));
  function build(event) {
    const points = (Array.isArray(event?.locations) ? event.locations : [])
      .filter(p => typeof p?.lat === 'number' && Number.isFinite(p.lat) &&
        typeof p?.lng === 'number' && Number.isFinite(p.lng) &&
        p.lat >= -90 && p.lat <= 90 && p.lng >= -180 && p.lng < 180);
    if (!points.length) return null;
    const point = (tag, p) => '    <' + tag + ' lat="' + p.lat.toFixed(7) +
      '" lon="' + p.lng.toFixed(7) + '"><name>' + xml(p.name || '未命名地點') +
      '</name></' + tag + '>';
    return [
      '<?xml version="1.0" encoding="UTF-8"?>',
      '<gpx version="1.1" creator="pokemon-go-special-events" xmlns="http://www.topografix.com/GPX/1/1">',
      '  <metadata><name>' + xml(event?.name || 'Pokémon GO 活動') + '</name></metadata>',
      ...points.map(p => point('wpt', p)),
      '  <trk>',
      '    <name>' + xml(event?.name || 'Pokémon GO 活動') + '</name>',
      '    <desc>依活動地點原順序連接；未進行道路導航或最佳路線規劃。</desc>',
      '    <trkseg>',
      ...points.map(p => point('trkpt', p)),
      '    </trkseg>',
      '  </trk>',
      '</gpx>'
    ].join('\n');
  }
  function download(event) {
    const content = build(event);
    if (!content) return false;
    const url = URL.createObjectURL(new Blob([content], {type:'application/gpx+xml;charset=utf-8'}));
    const link = document.createElement('a');
    link.href = url;
    link.download = (event?.id || 'event') + '.gpx';
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 30000);
    return true;
  }
  root.GpxExport = {build, download};
})(window);
