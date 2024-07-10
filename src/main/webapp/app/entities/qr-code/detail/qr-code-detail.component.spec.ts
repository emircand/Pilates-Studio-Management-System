import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { QRCodeDetailComponent } from './qr-code-detail.component';

describe('QRCode Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QRCodeDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: QRCodeDetailComponent,
              resolve: { qRCode: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(QRCodeDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load qRCode on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', QRCodeDetailComponent);

      // THEN
      expect(instance.qRCode).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
